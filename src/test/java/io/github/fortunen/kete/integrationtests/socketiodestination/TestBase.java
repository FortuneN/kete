package io.github.fortunen.kete.integrationtests.socketiodestination;

import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.socketio.SocketIODestination;
import io.github.fortunen.kete.destinations.socketio.SocketIODestinationConfig;

public class TestBase {

	private static final int SERVER_PORT = 3000;
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final ImageFromDockerfile SOCKETIO_IMAGE = new ImageFromDockerfile("kete-socketio-test", false)
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

	private static final String SERVER_TLS_JS = """
		const fs = require('fs');
		const { createServer } = require('https');
		const http = require('http');
		const { Server } = require('socket.io');
		const express = require('express');

		const requireClientAuth = fs.existsSync('/app/certs/ca.crt');

		const tlsOptions = {
		  key: fs.readFileSync('/app/certs/server.key'),
		  cert: fs.readFileSync('/app/certs/server.crt'),
		  ...(requireClientAuth && {
		    ca: [fs.readFileSync('/app/certs/ca.crt')],
		    requestCert: true,
		    rejectUnauthorized: true
		  })
		};

		const app = express();
		const httpsServer = createServer(tlsOptions, app);
		const io = new Server(httpsServer, {
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

		const httpApp = express();
		httpApp.get('/events', (req, res) => res.json(events));
		const httpServer = http.createServer(httpApp);
		httpServer.listen(3001, '0.0.0.0', () => {
		  console.log('HTTP events endpoint on port 3001');
		});

		httpsServer.listen(3000, '0.0.0.0', () => {
		  console.log('Socket.IO TLS server started on port 3000');
		});

		process.on('uncaughtException', (err) => {
		  console.error('Uncaught exception:', err);
		});
		""";

	private static final int EVENTS_PORT = 3001;

	protected GenericContainer<?> serverContainer;
	protected SocketIODestination destination;
	protected SocketIODestinationConfig config;
	private boolean tlsMode;

	@BeforeEach
	void setUp() {
		destination = new SocketIODestination();
		config = new SocketIODestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (serverContainer != null) {
			try { serverContainer.stop(); } catch (Exception e) { /* ignore */ }
			serverContainer = null;
		}
	}

	@SuppressWarnings("resource")
	protected void startServer() {
		tlsMode = false;
		serverContainer = new GenericContainer<>(SOCKETIO_IMAGE)
			.withExposedPorts(SERVER_PORT)
			.withCopyToContainer(Transferable.of(SERVER_JS, 0777), "/app/server.js")
			.withCommand("node", "/app/server.js");
		serverContainer.start();

		waitForServerReady();
	}

	@SuppressWarnings("resource")
	protected void startServerWithTls(TlsMaterial tls, boolean requireClientAuth) {
		tlsMode = true;
		var builder = new GenericContainer<>(SOCKETIO_IMAGE)
			.withExposedPorts(SERVER_PORT, EVENTS_PORT)
			.withCopyToContainer(Transferable.of(SERVER_TLS_JS, 0777), "/app/server.js")
			.withCopyToContainer(Transferable.of(tls.getServerCertificatePemBytes(), 0777), "/app/certs/server.crt")
			.withCopyToContainer(Transferable.of(tls.getServerPrivateKeyPemBytes(), 0777), "/app/certs/server.key");

		if (requireClientAuth) {
			builder.withCopyToContainer(Transferable.of(tls.getCaCertificatePemBytes(), 0777), "/app/certs/ca.crt");
		}

		serverContainer = builder.withCommand("node", "/app/server.js");
		serverContainer.start();

		waitForEventsEndpointReady();
	}

	protected String getServerUrl() {
		return "http://127.0.0.1:" + serverContainer.getMappedPort(SERVER_PORT);
	}

	protected String getServerTlsUrl() {
		return "https://127.0.0.1:" + serverContainer.getMappedPort(SERVER_PORT);
	}

	protected void configureDestination() {
		configureDestination(Map.of());
	}

	protected void configureDestination(Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put(Constants.KIND, "socketio");
		map.put("url", getServerUrl());
		map.put("event-name", "keycloak-event");
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put(Constants.KIND, "socketio");
		map.put("url", getServerTlsUrl());
		map.put("event-name", "keycloak-event");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithMtls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put(Constants.KIND, "socketio");
		map.put("url", getServerTlsUrl());
		map.put("event-name", "keycloak-event");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-path");
		map.put("tls.key-store.loader.path", tls.getKeyStoreFilePath());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected List<String> getReceivedEvents() throws Exception {
		var eventsUrl = tlsMode
			? "http://127.0.0.1:" + serverContainer.getMappedPort(EVENTS_PORT) + "/events"
			: getServerUrl() + "/events";
		var client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(5))
			.build();
		var request = HttpRequest.newBuilder()
			.uri(URI.create(eventsUrl))
			.timeout(Duration.ofSeconds(5))
			.GET()
			.build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			return List.of();
		}
		var root = MAPPER.readTree(response.body());
		var events = new ArrayList<String>();
		for (var node : root) {
			events.add(node.asText());
		}
		return events;
	}

	protected String getContainerLogs() {
		return serverContainer != null ? serverContainer.getLogs() : "(no container)";
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}

	private void waitForServerReady() {
		var url = "http://127.0.0.1:" + serverContainer.getMappedPort(SERVER_PORT) + "/events";
		var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build();
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
				var response = client.send(request, HttpResponse.BodyHandlers.discarding());
				return response.statusCode() == 200;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void waitForEventsEndpointReady() {
		var url = "http://127.0.0.1:" + serverContainer.getMappedPort(EVENTS_PORT) + "/events";
		var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build();
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
				var response = client.send(request, HttpResponse.BodyHandlers.discarding());
				return response.statusCode() == 200;
			} catch (Exception e) {
				return false;
			}
		});
	}
}
