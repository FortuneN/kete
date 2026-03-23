package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;

class SocketIODestinationE2ETests extends EndToEndTestBase {

	private static final int SERVER_PORT = 3000;

	private static final ImageFromDockerfile SOCKETIO_IMAGE = new ImageFromDockerfile("kete-socketio-e2e-test", false)
		.withDockerfileFromBuilder(builder -> builder
			.from("node:20-alpine")
			.workDir("/app")
			.run("npm init -y && npm install socket.io@4 express@4")
			.build()
		);

	private static final String SERVER_JS = """
		const { createServer } = require('http');
		const { Server } = require('socket.io');
		const express = require('express');

		const app = express();
		const httpServer = createServer(app);
		const io = new Server(httpServer, {
		  cors: { origin: '*' },
		  allowEIO3: true,
		  transports: ['websocket', 'polling']
		});

		const events = [];

		io.on('connection', (socket) => {
		  console.log('Client connected: ' + socket.id);
		  socket.onAny((eventName, data) => {
		    console.log('Event: ' + eventName);
		    events.push(typeof data === 'object' ? JSON.stringify(data) : String(data));
		  });
		});

		app.get('/events', (req, res) => res.json(events));

		httpServer.listen(3000, '0.0.0.0', () => {
		  console.log('Socket.IO server started on port 3000');
		});

		process.on('uncaughtException', (err) => {
		  console.error('Uncaught exception:', err);
		});
		""";

	private GenericContainer<?> socketioServer;

	@AfterEach
	void tearDown() {
		if (socketioServer != null) {
			try { socketioServer.stop(); } catch (Exception e) { /* ignore */ }
			socketioServer = null;
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToSocketIO() throws Exception {

		// arrange — start Socket.IO server on Docker network

		socketioServer = new GenericContainer<>(SOCKETIO_IMAGE)
			.withNetwork(createNetwork())
			.withNetworkAliases("socketio-server")
			.withExposedPorts(SERVER_PORT)
			.withCopyToContainer(Transferable.of(SERVER_JS, 0777), "/app/server.js")
			.withCommand("node", "/app/server.js");
		socketioServer.start();

		waitForHttpReady(socketioServer, SERVER_PORT, "/events");

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.socketio-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.socketio-test.destination.kind", "socketio");
		envVars.put("kete.routes.socketio-test.destination.url", "http://socketio-server:" + SERVER_PORT);
		envVars.put("kete.routes.socketio-test.destination.event-name", "keycloak-event");
		envVars.put("kete.routes.socketio-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — query the Socket.IO server directly to verify the event was received

				var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build();
				var baseUrl = "http://127.0.0.1:" + socketioServer.getMappedPort(SERVER_PORT);

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).ignoreExceptions().until(() -> {
					var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/events")).timeout(Duration.ofSeconds(5)).GET().build();
					var response = client.send(request, HttpResponse.BodyHandlers.ofString());
					return response.statusCode() == 200 && !response.body().equals("[]");
				});

				var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/events")).timeout(Duration.ofSeconds(5)).GET().build();
				var response = client.send(request, HttpResponse.BodyHandlers.ofString());

				assertThat(response.statusCode()).isEqualTo(200);
				assertThat(response.body()).satisfiesAnyOf(
					b -> assertThat(b).contains("type"),
					b -> assertThat(b).contains("operationType")
				);
				assertThat(response.body()).contains(TEST_REALM);

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}
}
