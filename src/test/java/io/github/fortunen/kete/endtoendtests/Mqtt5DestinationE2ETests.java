package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

class Mqtt5DestinationE2ETests extends EndToEndTestBase {

	private static final String TOPIC = "keycloak/events";
	private static final int MQTT_PORT = 1883;
	private GenericContainer<?> mosquitto;

	@AfterEach
	void tearDown() {
		if (mosquitto != null) {
			mosquitto.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToMqtt5Broker() throws Exception {

		// arrange

		// Create in-memory mosquitto config
		var configContent = "listener 1883\nallow_anonymous true\n".getBytes(StandardCharsets.UTF_8);

		mosquitto = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0")).withNetwork(createNetwork()).withNetworkAliases("mosquitto").withExposedPorts(MQTT_PORT).withCommand("mosquitto", "-c", "/mosquitto-no-auth.conf").withCopyToContainer(Transferable.of(configContent, 0777), "/mosquitto-no-auth.conf");
		mosquitto.start();
		waitForMqttReady(mosquitto, MQTT_PORT);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.mqtt-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.mqtt-test.destination.kind", "mqtt-5");
		envVars.put("kete.routes.mqtt-test.destination.host", "mosquitto");
		envVars.put("kete.routes.mqtt-test.destination.port", String.valueOf(MQTT_PORT));
		envVars.put("kete.routes.mqtt-test.destination.topic", TOPIC);
		envVars.put("kete.routes.mqtt-test.serializer.kind", "yaml");

		var brokerUrl = String.format("tcp://%s:%d", "127.0.0.1", mosquitto.getMappedPort(MQTT_PORT));

		var receivedMessage = new AtomicReference<String>();

		MqttClient subscriber = new MqttClient(brokerUrl, "test-subscriber", new MemoryPersistence());
		try {
			var options = new MqttConnectionOptions();
			options.setCleanStart(true);
			subscriber.connect(options);

			var subscription = new MqttSubscription(TOPIC, 1);
			IMqttMessageListener messageListener = (topic, msg) -> {
				receivedMessage.set(new String(msg.getPayload()));
			};
			subscriber.subscribe(new MqttSubscription[] { subscription }, new IMqttMessageListener[] { messageListener });

			try (var keycloak = createKeycloakContainer(envVars)) {
				keycloak.start();

				try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
					createTestRealm(adminClient);

					// act

					triggerLoginEvent(keycloak);

					// assert

					await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> receivedMessage.get() != null);

					var body = receivedMessage.get();
					// YAML serializer assertions - check for YAML format markers
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("type:"),
						b -> assertThat(b).contains("operationType:")
					);
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("realmName:"),
						b -> assertThat(b).contains("realmId:")
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
