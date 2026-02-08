package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

class Mqtt3DestinationE2ETests extends EndToEndTestBase {

	private static final String TOPIC_TEMPLATE = "keycloak/${kindLowerCase}";
	private static final int MQTT_PORT = 1883;
	private GenericContainer<?> mosquitto;

	@AfterEach
	void tearDown() {
		if (mosquitto != null) {
			mosquitto.stop();
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToMqtt3Broker() throws Exception {

		// arrange

		// Create in-memory mosquitto config
		var configContent = "listener 1883\nallow_anonymous true\n".getBytes(StandardCharsets.UTF_8);

		mosquitto = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0")).withNetwork(createNetwork()).withNetworkAliases("mosquitto").withExposedPorts(MQTT_PORT).withCommand("mosquitto", "-c", "/mosquitto-no-auth.conf").withCopyToContainer(Transferable.of(configContent, 0777), "/mosquitto-no-auth.conf");
		mosquitto.start();
		waitForMqttReady(mosquitto, MQTT_PORT);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.mqtt-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.mqtt-test.destination.kind", "mqtt-3");
		envVars.put("kete.routes.mqtt-test.destination.host", "mosquitto");
		envVars.put("kete.routes.mqtt-test.destination.port", String.valueOf(MQTT_PORT));
		envVars.put("kete.routes.mqtt-test.destination.topic", TOPIC_TEMPLATE);
		envVars.put("kete.routes.mqtt-test.serializer.kind", "toml");

		var brokerUrl = String.format("tcp://%s:%d", mosquitto.getHost(), mosquitto.getMappedPort(MQTT_PORT));

		var receivedMessage = new AtomicReference<String>();

		MqttClient subscriber = new MqttClient(brokerUrl, "test-subscriber", new MemoryPersistence());
		try {
			var options = new MqttConnectOptions();
			options.setCleanSession(true);
			subscriber.connect(options);

			subscriber.subscribe("keycloak/#", 1, (topic, msg) -> {
				receivedMessage.set(new String(msg.getPayload()));
			});

			try (var keycloak = createKeycloakContainer(envVars)) {
				keycloak.start();

				try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
					createTestRealm(adminClient);

					// act

					triggerLoginEvent(keycloak);

					// assert

					await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);

					var body = receivedMessage.get();
					// TOML serializer assertions - check for TOML format (key = "value")
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).matches("(?s).*type\\s*=.*"),
						b -> assertThat(b).matches("(?s).*operationType\\s*=.*")
					);
					assertThat(body).contains(TEST_REALM);

					// cleanup
					cleanupTestRealm(adminClient);
				}
			}
		} finally {
			if (subscriber.isConnected()) {
				subscriber.disconnect();
			}
			subscriber.close();
		}
	}
}
