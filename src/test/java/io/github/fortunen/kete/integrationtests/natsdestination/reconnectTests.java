package io.github.fortunen.kete.integrationtests.natsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.EventMessage;
import io.nats.client.Connection;

public class reconnectTests extends TestBase {

	private void arrangeDestination() throws Exception {

		startNats();

		var map = new HashMap<String, Object>();
		map.put("servers", getNatsUrl());
		map.put("subject", "test-subject");
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
		try (var subscriber = createSubscriber("test-subject", collector)) {

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
		try (var subscriber = createSubscriber("test-subject", collector)) {

			Thread.sleep(500);

			destination.send(createLoginMessage("evt-001"));

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == 1);

			// act

			destination.getConnection().close();

			destination.send(createLoginMessage("evt-002"));

			// assert

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == 2);

			assertThat(collector.getMessages()).hasSize(2);
		}
	}
}
