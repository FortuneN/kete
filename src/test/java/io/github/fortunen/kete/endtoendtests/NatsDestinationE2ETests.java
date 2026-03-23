package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.net.HttpURLConnection;
import java.net.URI;

class NatsDestinationE2ETests extends EndToEndTestBase {

	private static final String NATS_SUBJECT = "keycloak.events";
	private static final int NATS_PORT = 4222;
	private static final int NATS_MONITORING_PORT = 8222;
	private GenericContainer<?> natsServer;

	@AfterEach
	void tearDown() {
		if (natsServer != null) {
			natsServer.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToNatsSubject() throws Exception {

		// arrange

		natsServer = new GenericContainer<>(DockerImageName.parse("nats:2.10-alpine"))
			.withNetwork(createNetwork())
			.withNetworkAliases("nats")
			.withExposedPorts(NATS_PORT, NATS_MONITORING_PORT)
			.withCommand("--http_port", "8222");
		natsServer.start();

		waitForNatsReady(natsServer);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.nats-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.nats-test.destination.kind", "nats");
		envVars.put("kete.routes.nats-test.destination.servers", "nats://nats:" + NATS_PORT);
		envVars.put("kete.routes.nats-test.destination.subject", NATS_SUBJECT);
		envVars.put("kete.routes.nats-test.destination.authentication-method", "none");
		envVars.put("kete.routes.nats-test.serializer.kind", "json");

		var receivedMessages = new ArrayBlockingQueue<String>(10);

		// Set up NATS subscriber
		var natsUrl = "nats://" + "127.0.0.1" + ":" + natsServer.getMappedPort(NATS_PORT);
		var options = new Options.Builder()
			.server(natsUrl)
			.connectionTimeout(Duration.ofSeconds(10))
			.build();

		Connection connection = Nats.connect(options);
		var dispatcher = connection.createDispatcher((msg) -> {
			var body = new String(msg.getData(), StandardCharsets.UTF_8);
			receivedMessages.offer(body);
		});
		dispatcher.subscribe(NATS_SUBJECT);

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert - wait for message using Awaitility

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> !receivedMessages.isEmpty());

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
			try {
				connection.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void waitForNatsReady(GenericContainer<?> container) {
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
				try {
					var url = "http://" + "127.0.0.1" + ":" + container.getMappedPort(NATS_MONITORING_PORT) + "/varz";
					var conn = URI.create(url).toURL().openConnection();
					conn.setConnectTimeout(1000);
					conn.setReadTimeout(1000);
					conn.connect();
					var responseCode = ((HttpURLConnection) conn).getResponseCode();
					return responseCode == 200;
				} catch (Exception e) {
					return false;
				}
			});
	}
}
