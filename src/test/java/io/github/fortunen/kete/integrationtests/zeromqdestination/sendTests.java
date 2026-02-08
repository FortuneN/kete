package io.github.fortunen.kete.integrationtests.zeromqdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.zeromq.ZMQ;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_PublishSubscribe() throws Exception {

		// arrange

		var port = findAvailablePort();
		var endpoint = "tcp://*:" + port;

		var map = new HashMap<String, Object>();
		map.put("endpoint", endpoint);
		map.put("socket-type", "PUBLISH");
		map.put("connection-mode", "BIND");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();
		var subscriber = createSubscriber(port, null);

		Thread.sleep(500);

		var message = createMessage(
			"test-event-id",
			"test-realm",
			false,
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
			null,
			null
		);

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(100)).until(() -> {
			var data = subscriber.recv(ZMQ.NOBLOCK);
			if (data != null) {
				receivedMessage.set(data);
				return true;
			}
			return false;
		});

		assertThat(receivedMessage.get()).isNotNull();
		assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_PushPull() throws Exception {

		// arrange

		var port = findAvailablePort();
		var endpoint = "tcp://*:" + port;

		var map = new HashMap<String, Object>();
		map.put("endpoint", endpoint);
		map.put("socket-type", "PUSH");
		map.put("connection-mode", "BIND");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();
		var consumer = createPullConsumer(port, null);

		Thread.sleep(500);

		var message = createMessage(
			"test-event-id",
			"test-realm",
			false,
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
			null,
			null
		);

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(100)).until(() -> {
			var data = consumer.recv(ZMQ.NOBLOCK);
			if (data != null) {
				receivedMessage.set(data);
				return true;
			}
			return false;
		});

		assertThat(receivedMessage.get()).isNotNull();
		assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_ConnectMode() throws Exception {

		// arrange

		var port = findAvailablePort();
		var bindEndpoint = "tcp://*:" + port;
		var connectEndpoint = "tcp://localhost:" + port;

		// consumer binds first (acts as server)

		var consumer = createPullConsumerThatBinds(bindEndpoint);

		Thread.sleep(500);

		var map = new HashMap<String, Object>();
		map.put("endpoint", connectEndpoint);
		map.put("socket-type", "PUSH");
		map.put("connection-mode", "CONNECT");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		Thread.sleep(500);

		var receivedMessage = new AtomicReference<byte[]>();

		var message = createMessage(
			"test-event-id",
			"test-realm",
			false,
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
			null,
			null
		);

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(100)).until(() -> {
			var data = consumer.recv(ZMQ.NOBLOCK);
			if (data != null) {
				receivedMessage.set(data);
				return true;
			}
			return false;
		});

		assertThat(receivedMessage.get()).isNotNull();
		assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
