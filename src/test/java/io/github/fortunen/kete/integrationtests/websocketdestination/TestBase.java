package io.github.fortunen.kete.integrationtests.websocketdestination;

import static org.awaitility.Awaitility.await;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.websocket.WebSocketDestination;
import io.github.fortunen.kete.destinations.websocket.WebSocketDestinationConfig;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int WEBSOCKET_PORT = 8080;
	protected static final int WEBSOCKET_TLS_PORT = 8443;

	protected GenericContainer<?> container;
	protected GenericContainer<?> nginxProxy;
	protected Network network;
	protected WebSocketDestination destination;
	protected WebSocketDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new WebSocketDestination();
		config = new WebSocketDestinationConfig();
	}

	@AfterEach
	void tearDown() throws Exception {
		cleanUpContainers();
		if (destination != null) {
			try {
				destination.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected GenericContainer<?> startWebSocketEchoServer() throws Exception {

		cleanUpContainers();

		// Use websocket echo server image
		container = new GenericContainer<>(DockerImageName.parse("jmalloc/echo-server:0.3.6"))
			.withExposedPorts(WEBSOCKET_PORT)
			.withEnv("PORT", String.valueOf(WEBSOCKET_PORT))
			.waitingFor(Wait.forHttp("/").forPort(WEBSOCKET_PORT).forStatusCode(200))
			.withStartupTimeout(Duration.ofMinutes(1));

		container.start();

		waitForWebSocketReady();

		return container;
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

		// Create a Docker network for communication between containers
		network = Network.newNetwork();

		// Start the echo server without TLS (nginx will handle TLS termination)
		// The echo-server is only accessible within the Docker network
		container = new GenericContainer<>(DockerImageName.parse("jmalloc/echo-server:0.3.6"))
			.withNetwork(network)
			.withNetworkAliases("echo-server")
			.withExposedPorts(WEBSOCKET_PORT)
			.withEnv("PORT", String.valueOf(WEBSOCKET_PORT))
			.waitingFor(Wait.forListeningPort())
			.withStartupTimeout(Duration.ofMinutes(1));

		container.start();

		// Read certificate and key files (PEM format required for nginx)
		var serverCertBytes = Files.readAllBytes(Path.of(tls.getServerCertificatePemFilePath()));
		var serverKeyBytes = Files.readAllBytes(Path.of(tls.getServerPrivateKeyPemFilePath()));
		var caCertBytes = Files.readAllBytes(Path.of(tls.getCaCertificatePemFilePath()));

		// Create nginx config for WebSocket TLS termination
		var nginxConf = createNginxConfig(requireClientAuth);

		// Start nginx as TLS termination proxy
		// Put SSL files in /etc/nginx/ (not a subdirectory) to avoid directory creation issues
		nginxProxy = new GenericContainer<>(DockerImageName.parse("nginx:1.27-alpine"))
			.withNetwork(network)
			.withExposedPorts(WEBSOCKET_TLS_PORT)
			.withCopyToContainer(Transferable.of(nginxConf), "/etc/nginx/nginx.conf")
			.withCopyToContainer(Transferable.of(serverCertBytes), "/etc/nginx/server.crt")
			.withCopyToContainer(Transferable.of(serverKeyBytes), "/etc/nginx/server.key")
			.withCopyToContainer(Transferable.of(caCertBytes), "/etc/nginx/ca.crt")
			.waitingFor(Wait.forListeningPort())
			.withStartupTimeout(Duration.ofMinutes(1));

		nginxProxy.start();

		waitForWebSocketTlsReady(tls);
	}

	private String createNginxConfig(boolean requireClientAuth) {
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
			        server echo-server:%d;
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
			""".formatted(WEBSOCKET_PORT, WEBSOCKET_TLS_PORT, sslClientCertificate);
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
		return container.getMappedPort(WEBSOCKET_PORT);
	}

	protected int getWebSocketTlsPort() {
		return nginxProxy.getMappedPort(WEBSOCKET_TLS_PORT);
	}

	protected String getNginxHost() {
		return nginxProxy.getHost();
	}

	protected void waitForWebSocketReady() {
		await().atMost(Duration.ofSeconds(30))
			.pollInterval(Duration.ofSeconds(1))
			.until(() -> {
				try {
					var url = new java.net.URL("http://" + container.getHost() + ":" + getWebSocketPort() + "/");
					var conn = (java.net.HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(1000);
					conn.setReadTimeout(1000);
					return conn.getResponseCode() == 200;
				} catch (Exception e) {
					return false;
				}
			});
	}

	protected void waitForWebSocketTlsReady(TlsMaterial tls) {
		await().atMost(Duration.ofSeconds(30))
			.pollInterval(Duration.ofSeconds(1))
			.until(() -> {
				try {
					SSLContext sslContext = tls.getKeyStoreAndTrustStoreSSLContext();
					var url = new java.net.URL("https://" + getNginxHost() + ":" + getWebSocketTlsPort() + "/");
					var conn = (HttpsURLConnection) url.openConnection();
					conn.setSSLSocketFactory(sslContext.getSocketFactory());
					conn.setHostnameVerifier((hostname, session) -> true); // Disable hostname verification for test
					conn.setConnectTimeout(2000);
					conn.setReadTimeout(2000);
					return conn.getResponseCode() == 200;
				} catch (Exception e) {
					return false;
				}
			});
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "false", "", "");
	}
}
