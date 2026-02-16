package io.github.fortunen.kete.integrationtests.amqp1destination;

import static org.assertj.core.api.Assertions.assertThat;

import static org.awaitility.Awaitility.await;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import jakarta.jms.BytesMessage;
import jakarta.jms.Session;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startActiveMqArtemis();
		var queueName = "test-queue";
		var map = new HashMap<String, Object>();
		map.put("host", getHost());
		map.put("port", String.valueOf(getMappedPort()));
		map.put("destination-name", queueName);
		map.put("destination-type", "queue");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		// Set up consumer
		var factory = new JmsConnectionFactory("amqp://" + getHost() + ":" + getMappedPort());
		try (var connection = factory.createConnection();
			 var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

			connection.start();
			var queue = session.createQueue(queueName);
			var consumer = session.createConsumer(queue);

			consumer.setMessageListener(msg -> {
				try {
					if (msg instanceof BytesMessage bytesMessage) {
						var bytes = new byte[(int) bytesMessage.getBodyLength()];
						bytesMessage.readBytes(bytes);
						receivedMessage.set(bytes);
					}
				} catch (Exception ignored) {
				}
			});

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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithServerOnlyTLS(tls);
		var queueName = "test-queue";
		var map = new HashMap<String, Object>();
		map.put("host", getHost());
		map.put("port", String.valueOf(getAmqpsTlsPort()));
		map.put("destination-name", queueName);
		map.put("destination-type", "queue");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		// Set up consumer with TLS
		// NOTE: transport.verifyHost=false is required for tests because we connect to localhost/container IPs
		// but the server certificate's CN/SAN contains hostnames like "localhost", "127.0.0.1", etc.
		// The SERVER still enforces TLS - clients MUST provide valid certificates to connect.
		var amqpUrl = "amqps://" + getHost() + ":" + getAmqpsTlsPort()
			+ "?transport.trustStoreLocation=" + URLEncoder.encode(tls.getTrustStoreFilePath(), StandardCharsets.UTF_8)
			+ "&transport.trustStorePassword=" + URLEncoder.encode(tls.getTrustStorePassword(), StandardCharsets.UTF_8)
			+ "&transport.keyStoreLocation=" + URLEncoder.encode(tls.getKeyStoreFilePath(), StandardCharsets.UTF_8)
			+ "&transport.keyStorePassword=" + URLEncoder.encode(tls.getKeyStorePassword(), StandardCharsets.UTF_8)
			+ "&transport.verifyHost=false";
		var factory = new JmsConnectionFactory(amqpUrl);
		try (var connection = factory.createConnection();
			 var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

			connection.start();
			var queue = session.createQueue(queueName);
			var consumer = session.createConsumer(queue);

			consumer.setMessageListener(msg -> {
				try {
					if (msg instanceof BytesMessage bytesMessage) {
						var bytes = new byte[(int) bytesMessage.getBodyLength()];
						bytesMessage.readBytes(bytes);
						receivedMessage.set(bytes);
					}
				} catch (Exception ignored) {
				}
			});

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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithClientAndServerTLS(tls);
		var queueName = "test-queue";
		var map = new HashMap<String, Object>();
		map.put("host", getHost());
		map.put("port", String.valueOf(getAmqpsTlsPort()));
		map.put("destination-name", queueName);
		map.put("destination-type", "queue");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-base64");
		map.put("tls.trust-store.loader.base64", tls.getTrustStoreBase64());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-base64");
		map.put("tls.key-store.loader.base64", tls.getKeyStoreBase64());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		// Set up consumer with mTLS
		// NOTE: transport.verifyHost=false is required for tests because we connect to localhost/container IPs
		// but the server certificate's CN/SAN contains hostnames like "localhost", "127.0.0.1", etc.
		// The SERVER enforces mTLS via needClientAuth=true - clients MUST provide valid client certificates.
		var amqpUrl = "amqps://" + getHost() + ":" + getAmqpsTlsPort()
			+ "?transport.trustStoreLocation=" + URLEncoder.encode(tls.getTrustStoreFilePath(), StandardCharsets.UTF_8)
			+ "&transport.trustStorePassword=" + URLEncoder.encode(tls.getTrustStorePassword(), StandardCharsets.UTF_8)
			+ "&transport.keyStoreLocation=" + URLEncoder.encode(tls.getKeyStoreFilePath(), StandardCharsets.UTF_8)
			+ "&transport.keyStorePassword=" + URLEncoder.encode(tls.getKeyStorePassword(), StandardCharsets.UTF_8)
			+ "&transport.verifyHost=false";
		var factory = new JmsConnectionFactory(amqpUrl);
		try (var connection = factory.createConnection();
			 var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

			connection.start();
			var queue = session.createQueue(queueName);
			var consumer = session.createConsumer(queue);

			consumer.setMessageListener(msg -> {
				try {
					if (msg instanceof BytesMessage bytesMessage) {
						var bytes = new byte[(int) bytesMessage.getBodyLength()];
						bytesMessage.readBytes(bytes);
						receivedMessage.set(bytes);
					}
				} catch (Exception ignored) {
				}
			});

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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}
}
