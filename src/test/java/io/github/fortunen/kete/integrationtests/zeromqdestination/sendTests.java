package io.github.fortunen.kete.integrationtests.zeromqdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import zmq.util.Z85;

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

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
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

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
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

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
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
	public void shouldSend_CurveSecurity() throws Exception {

		// arrange — CURVE is ZeroMQ's encrypted + authenticated transport (equivalent of TLS)

		var port = findAvailablePort();

		var serverKeys = ZMQ.Curve.generateKeyPair();
		var clientKeys = ZMQ.Curve.generateKeyPair();

		// consumer binds as CURVE server

		var consumer = consumerContext.createSocket(SocketType.PULL);
		consumer.setCurveServer(true);
		consumer.setCurvePublicKey(Z85.decode(serverKeys.publicKey));
		consumer.setCurveSecretKey(Z85.decode(serverKeys.secretKey));
		consumer.setReceiveTimeOut(10_000);
		consumer.bind("tcp://*:" + port);

		Thread.sleep(500);

		// destination connects as CURVE client

		var map = new HashMap<String, Object>();
		map.put("endpoint", "tcp://localhost:" + port);
		map.put("socket-type", "PUSH");
		map.put("connection-mode", "CONNECT");
		map.put("authentication-type", "curve");
		map.put("curve.loader.kind", "z85-inline");
		map.put("curve.server-key", serverKeys.publicKey);
		map.put("curve.public-key", clientKeys.publicKey);
		map.put("curve.secret-key", clientKeys.secretKey);
		configureDestination(new MapConfiguration(map));
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

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
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
	public void shouldSend_PublishSubscribeWithEnvelope() throws Exception {

		// arrange — envelope is ZeroMQ's multipart framing for PUB/SUB topic filtering

		var port = findAvailablePort();

		var map = new HashMap<String, Object>();
		map.put("endpoint", "tcp://*:" + port);
		map.put("socket-type", "PUBLISH");
		map.put("connection-mode", "BIND");
		map.put("envelope", "keycloak-events");
		configureDestination(new MapConfiguration(map));
		destination.initialize();

		var subscriber = consumerContext.createSocket(SocketType.SUB);
		subscriber.setReceiveTimeOut(10_000);
		subscriber.subscribe("keycloak-events");
		subscriber.connect("tcp://localhost:" + port);

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

		// assert — verify both envelope frame and body frame

		var envelopeFrame = new AtomicReference<byte[]>();
		var bodyFrame = new AtomicReference<byte[]>();

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			var data = subscriber.recv(ZMQ.NOBLOCK);
			if (data != null) {
				envelopeFrame.set(data);
				if (subscriber.hasReceiveMore()) {
					bodyFrame.set(subscriber.recv(0));
				}
				return true;
			}
			return false;
		});

		assertThat(new String(envelopeFrame.get(), StandardCharsets.UTF_8)).isEqualTo("keycloak-events");
		assertThat(new String(bodyFrame.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
