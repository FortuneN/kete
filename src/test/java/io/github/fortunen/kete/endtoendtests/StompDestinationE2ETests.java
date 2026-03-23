package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

class StompDestinationE2ETests extends EndToEndTestBase {

	private static final String STOMP_DESTINATION = "/queue/keycloak-events";
	private static final int STOMP_PORT = 61613;
	private GenericContainer<?> activemq;

	@AfterEach
	void tearDown() {
		if (activemq != null) {
			activemq.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToStompQueue() throws Exception {

		// arrange

		activemq = new GenericContainer<>(DockerImageName.parse("apache/activemq-classic:6.1.6"))
			.withNetwork(createNetwork())
			.withNetworkAliases("activemq")
			.withExposedPorts(STOMP_PORT, 61616);
		activemq.start();
		waitForStompReady(activemq, STOMP_PORT);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.stomp-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.stomp-test.destination.kind", "stomp");
		envVars.put("kete.routes.stomp-test.destination.host", "activemq");
		envVars.put("kete.routes.stomp-test.destination.port", String.valueOf(STOMP_PORT));
		envVars.put("kete.routes.stomp-test.destination.destination", STOMP_DESTINATION);
		envVars.put("kete.routes.stomp-test.serializer.kind", "properties");

		var receivedMessages = new LinkedBlockingQueue<String>();

		// Set up STOMP subscriber
		var connection = new StompConnection();
		connection.open("127.0.0.1", activemq.getMappedPort(STOMP_PORT));
		connection.connect("admin", "admin");

		var headers = new HashMap<String, String>();
		headers.put("id", "sub-0");
		connection.subscribe(STOMP_DESTINATION, "auto", headers);

		// Start reader thread
		var running = new AtomicBoolean(true);
		var readerThread = new Thread(() -> {
			while (running.get()) {
				try {
					StompFrame frame = connection.receive(1000);
					if (frame != null && "MESSAGE".equals(frame.getAction())) {
						receivedMessages.offer(new String(frame.getContent()));
					}
				} catch (Exception e) {
					if (running.get()) {
						// ignore timeout
					}
				}
			}
		});
		readerThread.setDaemon(true);
		readerThread.start();

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert - wait for message using Awaitility

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> !receivedMessages.isEmpty());

				var body = receivedMessages.poll(1, TimeUnit.SECONDS);

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
		} finally {
			running.set(false);
			try {
				connection.disconnect();
			} catch (Exception e) {
				// ignore
			}
			try {
				connection.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void waitForStompReady(GenericContainer<?> container, int port) {
		var mappedPort = container.getMappedPort(port);
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
				try {
					var conn = new StompConnection();
					conn.open("127.0.0.1", mappedPort);
					conn.connect("", "");
					conn.disconnect();
					conn.close();
					return true;
				} catch (Exception e) {
					return false;
				}
			});
	}
}
