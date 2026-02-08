package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;

class WebSocketDestinationE2ETests extends EndToEndTestBase {

	private TestWebSocketServer wsServer;
	private LinkedBlockingQueue<String> receivedMessages;

	@BeforeEach
	void setUp() throws Exception {
		receivedMessages = new LinkedBlockingQueue<>();
		wsServer = new TestWebSocketServer(0, receivedMessages); // port 0 = auto-assign
		wsServer.start();

		// Wait for the server to start
		await().atMost(Duration.ofSeconds(10)).until(() -> wsServer.getPort() > 0);

		// Expose the port to Docker containers
		Testcontainers.exposeHostPorts(wsServer.getPort());
	}

	@AfterEach
	void tearDown() throws Exception {
		if (wsServer != null) {
			wsServer.stop(1000);
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToWebSocket() throws Exception {

		// arrange

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.websocket-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.websocket-test.destination.kind", "websocket");
		envVars.put("kete.routes.websocket-test.destination.host", "host.testcontainers.internal");
		envVars.put("kete.routes.websocket-test.destination.port", String.valueOf(wsServer.getPort()));
		envVars.put("kete.routes.websocket-test.destination.path", "/events");
		envVars.put("kete.routes.websocket-test.serializer.kind", "properties");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert - wait for message from Keycloak via WebSocket

				await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> !receivedMessages.isEmpty());

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
		}
	}

	private static class TestWebSocketServer extends WebSocketServer {
		private final LinkedBlockingQueue<String> messages;

		public TestWebSocketServer(int port, LinkedBlockingQueue<String> messages) {
			super(new InetSocketAddress(port));
			this.messages = messages;
			setReuseAddr(true);
		}

		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			// Connection opened
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			// Connection closed
		}

		@Override
		public void onMessage(WebSocket conn, String message) {
			messages.offer(message);
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			// Handle error
		}

		@Override
		public void onStart() {
			// Server started
		}
	}
}
