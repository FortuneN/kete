package io.github.fortunen.kete.integrationtests.websocketdestination;

import static org.awaitility.Awaitility.await;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration2.MapConfiguration;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.websocket.WebSocketDestination;
import io.github.fortunen.kete.destinations.websocket.WebSocketDestinationConfig;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int WEBSOCKET_TLS_PORT = 8443;

	protected GenericContainer<?> container;
	protected GenericContainer<?> nginxProxy;
	protected Network network;
	protected WebSocketDestination destination;
	protected WebSocketDestinationConfig config;
	protected TestWebSocketServer wsServer;
	protected LinkedBlockingQueue<String> receivedMessages;

	@BeforeEach
	void setUp() {
		destination = new WebSocketDestination();
		config = new WebSocketDestinationConfig();
		receivedMessages = new LinkedBlockingQueue<>();
	}

	@AfterEach
	void tearDown() throws Exception {
		cleanUpContainers();
		if (wsServer != null) {
			try {
				wsServer.stop(1000);
			} catch (Exception e) {
				// ignore
			}
			wsServer = null;
		}
		if (destination != null) {
			try {
				destination.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		mapConfig.setProperty(Constants.KIND, "websocket");
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected void startWebSocketServer() throws Exception {

		cleanUpContainers();

		wsServer = new TestWebSocketServer(0, receivedMessages);
		wsServer.start();

		await().atMost(Duration.ofSeconds(10)).until(() -> wsServer.getPort() > 0);
	}

	protected void startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		startWebSocketWithTls(tls, false);
	}

	protected void startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		startWebSocketWithTls(tls, true);
	}

	private void startWebSocketWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {

		cleanUpContainers();

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled");
		}

		// Start host-side WebSocket server that captures messages
		wsServer = new TestWebSocketServer(0, receivedMessages);
		wsServer.start();
		await().atMost(Duration.ofSeconds(10)).until(() -> wsServer.getPort() > 0);

		// Expose host-side server port to Docker containers
		Testcontainers.exposeHostPorts(wsServer.getPort());

		// Create a Docker network for nginx
		network = Network.newNetwork();

		// Create nginx config for WebSocket TLS termination, proxying to host-side server
		var nginxConf = createNginxConfig(requireClientAuth, wsServer.getPort());

		// Start nginx as TLS termination proxy
		nginxProxy = new GenericContainer<>(DockerImageName.parse("nginx:1.27-alpine"))
			.withNetwork(network)
			.withExposedPorts(WEBSOCKET_TLS_PORT)
			.withCopyToContainer(Transferable.of(nginxConf, 0777), "/etc/nginx/nginx.conf")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getServerCertificatePemFilePath())), 0777), "/etc/nginx/server.crt")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getServerPrivateKeyPemFilePath())), 0777), "/etc/nginx/server.key")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getCaCertificatePemFilePath())), 0777), "/etc/nginx/ca.crt")
			.waitingFor(Wait.forListeningPort())
			.withStartupTimeout(Duration.ofMinutes(10));

		nginxProxy.start();

		waitForWebSocketTlsReady(tls);
	}

	private String createNginxConfig(boolean requireClientAuth, int backendPort) {
		var sslClientCertificate = requireClientAuth
			? """
			        ssl_client_certificate /etc/nginx/ca.crt;
			        ssl_verify_client on;
			"""
			: "";

		return """
			events {
			    worker_connections 1024;
			}

			http {
			    upstream websocket {
			        server host.testcontainers.internal:%d;
			    }

			    server {
			        listen %d ssl;

			        ssl_certificate /etc/nginx/server.crt;
			        ssl_certificate_key /etc/nginx/server.key;
			%s
			        location / {
			            proxy_pass http://websocket;
			            proxy_http_version 1.1;
			            proxy_set_header Upgrade $http_upgrade;
			            proxy_set_header Connection "upgrade";
			            proxy_set_header Host $host;
			            proxy_read_timeout 86400;
			        }
			    }
			}
			""".formatted(backendPort, WEBSOCKET_TLS_PORT, sslClientCertificate);
	}

	protected void cleanUpContainers() {
		if (nginxProxy != null) {
			try {
				nginxProxy.stop();
			} catch (Exception e) {
				// ignore
			}
			nginxProxy = null;
		}
		if (container != null) {
			try {
				container.stop();
			} catch (Exception e) {
				// ignore
			}
			container = null;
		}
		if (network != null) {
			try {
				network.close();
			} catch (Exception e) {
				// ignore
			}
			network = null;
		}
	}

	protected int getWebSocketPort() {
		return wsServer.getPort();
	}

	protected int getWebSocketTlsPort() {
		return nginxProxy.getMappedPort(WEBSOCKET_TLS_PORT);
	}

	protected String getNginxHost() {
		return nginxProxy.getHost();
	}

	protected void waitForWebSocketTlsReady(TlsMaterial tls) {
		await().atMost(Duration.ofSeconds(30))
			.pollInterval(Duration.ofSeconds(1))
			.until(() -> {
				try {
					var sslContext = tls.getKeyStoreAndTrustStoreSSLContext();
					var factory = sslContext.getSocketFactory();
					try (var socket = factory.createSocket(getNginxHost(), getWebSocketTlsPort())) {
						socket.setSoTimeout(2000);
						return socket.isConnected();
					}
				} catch (Exception e) {
					return false;
				}
			});
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "false", "", "");
	}

	protected static class TestWebSocketServer extends WebSocketServer {

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
