package io.github.fortunen.kete.integrationtests.natsjetstreamdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.EventMessage;
import io.nats.client.Connection;

public class reconnectTests extends TestBase {

	private void arrangeDestination() throws Exception {

		startNatsJetStream();

		var map = new HashMap<String, Object>();
		map.put("servers", getNatsUrl());
		map.put("subject", "test.subject");
		map.put("stream", STREAM_NAME);
		map.put("authentication-method", "none");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();
	}

	private EventMessage createLoginMessage(String eventId) {
		return createMessage(
			eventId,
			"test-realm",
			false,
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
			null,
			null
		);
	}

	@Test
	public void shouldRecoverInBackgroundAfterTerminalConnectionClose() throws Exception {

		// arrange

		arrangeDestination();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriber("test.subject", collector)) {

			Thread.sleep(500);

			destination.send(createLoginMessage("evt-001"));

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == 1);

			// act

			var staleConnection = destination.getConnection();
			staleConnection.close();

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() ->
				destination.getConnection() != staleConnection
					&& destination.getConnection().getStatus() == Connection.Status.CONNECTED);

			destination.send(createLoginMessage("evt-002"));

			// assert

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == 2);

			assertThat(collector.getMessages()).hasSize(2);
		}
	}

	@Test
	public void shouldRecoverOnSendAfterTerminalConnectionClose() throws Exception {

		// arrange

		arrangeDestination();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriber("test.subject", collector)) {

			Thread.sleep(500);

			destination.send(createLoginMessage("evt-001"));

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == 1);

			// act

			destination.getConnection().close();

			destination.send(createLoginMessage("evt-002"));

			// assert

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == 2);

			assertThat(collector.getMessages()).hasSize(2);

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> getServerConnectionCount() == 2);
		}
	}

	private int getServerConnectionCount() throws Exception {

		var url = "http://127.0.0.1:" + container.getMappedPort(NATS_MONITORING_PORT) + "/connz";

		try (var stream = URI.create(url).toURL().openStream()) {

			var body = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			var matcher = Pattern.compile("\"num_connections\"\\s*:\\s*(\\d+)").matcher(body);

			if (!matcher.find()) {
				throw new IllegalStateException("num_connections not found in " + url + " response");
			}

			return Integer.parseInt(matcher.group(1));
		}
	}

	@Test
	public void shouldRecoverOnConcurrentSendsAfterTerminalConnectionClose() throws Exception {

		// arrange

		var sendCount = 8;

		arrangeDestination();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriber("test.subject", collector)) {

			Thread.sleep(500);

			// act

			destination.getConnection().close();

			try (var executor = Executors.newFixedThreadPool(sendCount)) {

				var futures = new ArrayList<Future<?>>();

				for (var i = 0; i < sendCount; i++) {
					var eventId = "evt-" + i;
					futures.add(executor.submit(() -> destination.send(createLoginMessage(eventId))));
				}

				for (var future : futures) {
					future.get();
				}
			}

			// assert

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == sendCount);

			assertThat(collector.getMessages()).hasSize(sendCount);
			assertThat(destination.getConnection().getStatus()).isEqualTo(Connection.Status.CONNECTED);
		}
	}
}
