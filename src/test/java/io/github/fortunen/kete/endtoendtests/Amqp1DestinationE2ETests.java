package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.jms.BytesMessage;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

@SuppressWarnings("resource")
class Amqp1DestinationE2ETests extends EndToEndTestBase {

	private static final String QUEUE_NAME = "keycloak-events";
	private static final int AMQP_PORT = 5672;
	private GenericContainer<?> artemis;

	@AfterEach
	void tearDown() {
		if (artemis != null) {
			artemis.stop();
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToAmqp1Queue() throws Exception {

		// arrange

		artemis = new GenericContainer<>(DockerImageName.parse("apache/activemq-artemis:2.31.2")).withNetwork(createNetwork()).withNetworkAliases("artemis").withExposedPorts(AMQP_PORT, 8161).withEnv("ARTEMIS_USER", "admin").withEnv("ARTEMIS_PASSWORD", "admin").withEnv("ANONYMOUS_LOGIN", "true");
		artemis.start();
		waitForAmqpReady(artemis, AMQP_PORT);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.amqp1-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.amqp1-test.destination.kind", "amqp-1");
		envVars.put("kete.routes.amqp1-test.destination.host", "artemis");
		envVars.put("kete.routes.amqp1-test.destination.port", String.valueOf(AMQP_PORT));
		envVars.put("kete.routes.amqp1-test.destination.username", "admin");
		envVars.put("kete.routes.amqp1-test.destination.password", "admin");
		envVars.put("kete.routes.amqp1-test.destination.destination-name", QUEUE_NAME);
		envVars.put("kete.routes.amqp1-test.serializer.kind", "properties");

		var brokerUrl = String.format("amqp://%s:%d", artemis.getHost(), artemis.getMappedPort(AMQP_PORT));
		var receivedMessage = new AtomicReference<String>();

		JmsConnectionFactory factory = new JmsConnectionFactory(brokerUrl);
		factory.setUsername("admin");
		factory.setPassword("admin");

		try (var connection = factory.createConnection()) {
			connection.start();
			var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			var queue = session.createQueue(QUEUE_NAME);
			var consumer = session.createConsumer(queue);

			// Set up async message listener
			consumer.setMessageListener(message -> {
				try {
					String body;
					if (message instanceof TextMessage textMessage) {
						body = textMessage.getText();
					} else if (message instanceof BytesMessage bytesMessage) {
						var bytes = new byte[(int) bytesMessage.getBodyLength()];
						bytesMessage.readBytes(bytes);
						body = new String(bytes, StandardCharsets.UTF_8);
					} else {
						body = message.toString();
					}
					receivedMessage.set(body);
				} catch (Exception e) {
					// ignore
				}
			});

			try (var keycloak = createKeycloakContainer(envVars)) {
				keycloak.start();

				try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
					createTestRealm(adminClient);

					// act

					triggerLoginEvent(keycloak);

					// assert - wait for message using Awaitility

					await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);

					var body = receivedMessage.get();
					// Properties serializer assertions - check for key=value format
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("type="),
						b -> assertThat(b).contains("operationType=")
					);
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("realmName="),
						b -> assertThat(b).contains("realmId=")
					);
					assertThat(body).contains(TEST_REALM);

					// cleanup
					cleanupTestRealm(adminClient);
				}
			}
		}
	}
}
