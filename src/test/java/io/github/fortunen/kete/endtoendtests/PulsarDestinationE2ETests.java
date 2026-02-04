package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("resource")
class PulsarDestinationE2ETests extends EndToEndTestBase {

	private static final String PULSAR_TOPIC = "persistent://public/default/keycloak-events";
	private static final int PULSAR_PORT = 6650;
	private static final int PULSAR_HTTP_PORT = 8080;
	private GenericContainer<?> pulsarServer;

	@AfterEach
	void tearDown() {
		if (pulsarServer != null) {
			pulsarServer.stop();
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToPulsarTopic() throws Exception {

		// arrange

		pulsarServer = new GenericContainer<>(DockerImageName.parse("apachepulsar/pulsar:3.3.2"))
			.withNetwork(createNetwork())
			.withNetworkAliases("pulsar")
			.withExposedPorts(PULSAR_PORT, PULSAR_HTTP_PORT)
			.withCommand("/bin/bash", "-c", "bin/apply-config-from-env.py conf/standalone.conf && bin/pulsar standalone")
			.waitingFor(Wait.forLogMessage(".*messaging service is ready.*", 1))
			.withStartupTimeout(CONTAINER_STARTUP_TIMEOUT);
		pulsarServer.start();

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.pulsar-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.pulsar-test.destination.kind", "pulsar");
		envVars.put("kete.routes.pulsar-test.destination.service-url", "pulsar://pulsar:" + PULSAR_PORT);
		envVars.put("kete.routes.pulsar-test.destination.topic", PULSAR_TOPIC);
		envVars.put("kete.routes.pulsar-test.serializer.kind", "json");

		var receivedMessages = new ArrayBlockingQueue<String>(10);

		// Set up Pulsar subscriber
		var pulsarUrl = "pulsar://" + pulsarServer.getHost() + ":" + pulsarServer.getMappedPort(PULSAR_PORT);
		PulsarClient client = PulsarClient.builder()
			.serviceUrl(pulsarUrl)
			.connectionTimeout(10, TimeUnit.SECONDS)
			.operationTimeout(30, TimeUnit.SECONDS)
			.build();

		Consumer<byte[]> consumer = client.newConsumer()
			.topic(PULSAR_TOPIC)
			.subscriptionName("test-subscription")
			.subscribe();

		var subscriberThread = new Thread(() -> {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					var msg = consumer.receive(1, TimeUnit.SECONDS);
					if (msg != null) {
						var body = new String(msg.getData(), StandardCharsets.UTF_8);
						receivedMessages.offer(body);
						consumer.acknowledge(msg);
					}
				}
			} catch (Exception e) {
				// ignore
			}
		});
		subscriberThread.setDaemon(true);
		subscriberThread.start();

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert - wait for message using Awaitility

				await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> !receivedMessages.isEmpty());

				var body = receivedMessages.poll(1, TimeUnit.SECONDS);

				// JSON serializer assertions
				assertThat(body).isNotEmpty();
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("\"type\""),
					b -> assertThat(b).contains("\"operationType\"")
				);
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("\"realmId\""),
					b -> assertThat(b).contains("\"authDetails\"")
				);
				assertThat(body).contains(TEST_REALM);

				// cleanup
				cleanupTestRealm(adminClient);
			}
		} finally {
			subscriberThread.interrupt();
			try {
				consumer.close();
				client.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
