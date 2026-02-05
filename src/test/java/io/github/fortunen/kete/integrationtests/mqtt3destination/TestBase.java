package io.github.fortunen.kete.integrationtests.mqtt3destination;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import org.apache.commons.configuration2.MapConfiguration;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.mqtt3.Mqtt3Destination;
import io.github.fortunen.kete.destinations.mqtt3.Mqtt3DestinationConfig;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int MQTT_TLS_PORT = 8883;
	protected static final int MQTT_WS_PORT = 8000;

	protected HiveMQContainer container;
	protected Mqtt3Destination destination;
	protected Mqtt3DestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new Mqtt3Destination();
		config = new Mqtt3DestinationConfig();
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		mapConfig.setProperty(Constants.KIND, "mqtt-3");
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected HiveMQContainer startMqtt() throws Exception {

		cleanUpContainer();

		// Create HiveMQ config with WebSocket listener
		var configXml = createHiveMqConfigWithWebSocket();

		container = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:2024.3"))
			.withCopyToContainer(Transferable.of(configXml.getBytes(StandardCharsets.UTF_8), 0777), "/opt/hivemq/conf/config.xml")
			.withExposedPorts(1883, MQTT_WS_PORT);
		container.start();

		// Wait for MQTT broker to be fully ready to accept connections
		waitForMqttReady();

		return container;
	}

	protected HiveMQContainer startMqttWithTls(TlsMaterial tls) throws Exception {
		return startMqttWithTls(tls, false);
	}

	protected HiveMQContainer startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		return startMqttWithTls(tls, false);
	}

	protected HiveMQContainer startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		return startMqttWithTls(tls, true);
	}

	private HiveMQContainer startMqttWithTls(TlsMaterial tls, boolean requireClientCert) throws Exception {

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled (call withEnabled(true) when building TlsMaterial)");
		}

		if (tls.getKeyStoreAndTrustStoreSSLContext() == null) {
			throw new IllegalStateException("SSL context is null - ensure TLS is properly configured");
		}

		if (tls.getKeyStoreFilePath() == null) {
			throw new IllegalStateException("Key store file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		if (tls.getTrustStoreFilePath() == null) {
			throw new IllegalStateException("Trust store file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		cleanUpContainer();

		// Create HiveMQ TLS config.xml
		// clientAuthMode controls SERVER-SIDE enforcement:
		//   - "NONE": Server-only TLS - client trusts server but server doesn't require client cert
		//   - "REQUIRED": mTLS - server REQUIRES valid client certificate (enforced by HiveMQ)
		var clientAuthMode = requireClientCert ? "REQUIRED" : "NONE";
		var configXml = createHiveMqTlsConfig(
			tls.getKeyStorePassword(),
			tls.getKeyPassword(),
			tls.getTrustStorePassword(),
			clientAuthMode
		);

		// Read certificate files into memory
		var keystoreBytes = Files.readAllBytes(Path.of(tls.getServerKeyStoreFilePath()));
		var truststoreBytes = Files.readAllBytes(Path.of(tls.getTrustStoreFilePath()));

		container = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:2024.3"))
			.withCopyToContainer(Transferable.of(configXml.getBytes(StandardCharsets.UTF_8), 0777), "/opt/hivemq/conf/config.xml")
			.withCopyToContainer(Transferable.of(keystoreBytes, 0777), "/opt/hivemq/conf/keystore.jks")
			.withCopyToContainer(Transferable.of(truststoreBytes, 0777), "/opt/hivemq/conf/truststore.jks")
			.withExposedPorts(1883, MQTT_TLS_PORT);
		container.start();

		// Wait for MQTT broker to be fully ready to accept connections
		waitForMqttReady();

		return container;
	}

	private String createHiveMqTlsConfig(String keystorePassword, String keyPassword, String truststorePassword, String clientAuthMode) {
		return """
			<?xml version="1.0"?>
			<hivemq xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				<listeners>
					<tcp-listener>
						<port>1883</port>
						<bind-address>0.0.0.0</bind-address>
					</tcp-listener>
					<tls-tcp-listener>
						<port>8883</port>
						<bind-address>0.0.0.0</bind-address>
						<tls>
							<keystore>
								<path>/opt/hivemq/conf/keystore.jks</path>
								<password>%s</password>
								<private-key-password>%s</private-key-password>
							</keystore>
							<truststore>
								<path>/opt/hivemq/conf/truststore.jks</path>
								<password>%s</password>
							</truststore>
							<client-authentication-mode>%s</client-authentication-mode>
						</tls>
					</tls-tcp-listener>
				</listeners>
			</hivemq>
			""".formatted(
				keystorePassword != null ? keystorePassword : "",
				keyPassword != null ? keyPassword : "",
				truststorePassword != null ? truststorePassword : "",
				clientAuthMode
			);
	}

	private String createHiveMqConfigWithWebSocket() {
		return """
			<?xml version="1.0"?>
			<hivemq xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				<listeners>
					<tcp-listener>
						<port>1883</port>
						<bind-address>0.0.0.0</bind-address>
					</tcp-listener>
					<websocket-listener>
						<port>8000</port>
						<bind-address>0.0.0.0</bind-address>
						<path>/mqtt</path>
						<subprotocols>
							<subprotocol>mqttv3.1</subprotocol>
							<subprotocol>mqtt</subprotocol>
						</subprotocols>
						<allow-extensions>true</allow-extensions>
					</websocket-listener>
				</listeners>
			</hivemq>
			""";
	}

	protected int getMqttTlsPort() {
		return container.getMappedPort(MQTT_TLS_PORT);
	}

	protected int getWsPort() {
		return container.getMappedPort(MQTT_WS_PORT);
	}

	private void waitForMqttReady() throws Exception {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {

				var clientId = "readiness-probe-" + UUID.randomUUID().toString().substring(0, 8);
				var client = new MqttClient("tcp://" + container.getHost() + ":" + container.getMqttPort(), clientId, new MemoryPersistence());
				var options = new MqttConnectOptions();

				options.setCleanSession(true);
				options.setConnectionTimeout(5);
				client.connect(options);
				client.disconnect();
				client.close();

				return true;

			} catch (Exception e) {
				return false;
			}
		});
	}

	protected void cleanUpContainer() {

		if (container != null) {
			try {
				container.stop();
			} catch (Exception exception) {
				// ignore
			}
		}

		container = null;
	}

	protected void cleanUp() {

		if (destination != null) {
			try {
				destination.close();
			} catch (Exception exception) {
				// ignore
			}
		}

		destination = null;

		cleanUpContainer();
	}

	protected static EventMessage createMessage(
		String eventId,
		String realm,
		boolean isAdminEvent,
		String eventType,
		String contentType,
		byte[] eventBody,
		String resourceType,
		String operationType
	) {
		return new EventMessage(
			realm != null ? realm : "",
			eventId != null ? eventId : "",
			eventBody != null ? eventBody : EMPTY_BYTES,
			eventType != null ? eventType : "",
			contentType != null ? contentType : "",
			resourceType != null ? resourceType : "",
			isAdminEvent ? Constants.ADMIN_EVENT : Constants.EVENT,
			operationType != null ? operationType : "",
			"SUCCESS"
		);
	}

	protected interface MessageCallback {
		void messageArrived(String topic, MqttMessage message) throws Exception;
	}

	protected AutoCloseable createSubscriber(String topic, int qos, MessageCallback callback) throws Exception {

		var clientId = "test-subscriber-" + UUID.randomUUID().toString().substring(0, 8);
		var client = new MqttClient("tcp://" + container.getHost() + ":" + container.getMqttPort(), clientId, new MemoryPersistence());
		var options = new MqttConnectOptions();

		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		client.connect(options);
		client.subscribe(topic, qos, (t, msg) -> callback.messageArrived(t, msg));

		return () -> {
			try {
				if (client.isConnected()) {
					client.disconnect();
				}
				client.close();
			} catch (Exception ignored) {
			}
		};
	}

	protected AutoCloseable createSubscriberWithTls(String topic, int qos, TlsMaterial tls, MessageCallback callback) throws Exception {
		var clientId = "test-subscriber-tls-" + UUID.randomUUID().toString().substring(0, 8);
		var client = new MqttClient("ssl://" + container.getHost() + ":" + getMqttTlsPort(), clientId, new MemoryPersistence());
		var options = new MqttConnectOptions();
		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		options.setSocketFactory(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory());
		client.connect(options);
		client.subscribe(topic, qos, (t, msg) -> callback.messageArrived(t, msg));
		return () -> {
			try {
				if (client.isConnected()) {
					client.disconnect();
				}
				client.close();
			} catch (Exception ignored) {
			}
		};
	}

	@AfterEach
	void tearDown() {
		cleanUp();
	}
}
