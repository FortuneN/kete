package io.github.fortunen.kete.integrationtests.gcppubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.gcppubsub.GcpPubSubDestination;
import io.github.fortunen.kete.destinations.gcppubsub.GcpPubSubDestinationConfig;

public class TestBase {

	private static final String EMULATOR_IMAGE = "google/cloud-sdk:emulators";
	private static final String NGINX_IMAGE = "nginx:1.27-alpine";
	private static final int EMULATOR_PORT = 8085;
	private static final int NGINX_TLS_PORT = 8443;
	private static final ObjectMapper MAPPER = new ObjectMapper();

	protected static final String PROJECT_ID = "test-project";
	protected static final String TOPIC_ID = "test-topic";
	protected static final String SUBSCRIPTION_ID = "test-subscription";

	protected GenericContainer<?> emulator;
	protected GenericContainer<?> nginxProxy;
	protected Network network;
	protected GcpPubSubDestination destination;
	protected GcpPubSubDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new GcpPubSubDestination();
		config = new GcpPubSubDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (nginxProxy != null) {
			try { nginxProxy.stop(); } catch (Exception e) { /* ignore */ }
			nginxProxy = null;
		}
		if (emulator != null) {
			try { emulator.stop(); } catch (Exception e) { /* ignore */ }
			emulator = null;
		}
		if (network != null) {
			try { network.close(); } catch (Exception e) { /* ignore */ }
			network = null;
		}
	}

	@SuppressWarnings("resource")
	protected void startEmulator() {
		emulator = new GenericContainer<>(DockerImageName.parse(EMULATOR_IMAGE))
			.withExposedPorts(EMULATOR_PORT)
			.withCommand("gcloud", "beta", "emulators", "pubsub", "start",
				"--project=" + PROJECT_ID,
				"--host-port=0.0.0.0:" + EMULATOR_PORT);
		emulator.start();
		waitForEmulatorReady(getEmulatorBaseUrl());
	}

	@SuppressWarnings("resource")
	protected void startEmulatorOnNetwork() {
		network = Network.newNetwork();
		emulator = new GenericContainer<>(DockerImageName.parse(EMULATOR_IMAGE))
			.withNetwork(network)
			.withNetworkAliases("pubsub-emulator")
			.withExposedPorts(EMULATOR_PORT)
			.withCommand("gcloud", "beta", "emulators", "pubsub", "start",
				"--project=" + PROJECT_ID,
				"--host-port=0.0.0.0:" + EMULATOR_PORT);
		emulator.start();
		waitForEmulatorReady(getEmulatorBaseUrl());
	}

	@SuppressWarnings("resource")
	protected void startNginxTlsProxy(TlsMaterial tls, boolean requireClientAuth) throws Exception {
		var nginxConf = createNginxConfig(requireClientAuth);
		nginxProxy = new GenericContainer<>(DockerImageName.parse(NGINX_IMAGE))
			.withNetwork(network)
			.withExposedPorts(NGINX_TLS_PORT)
			.withCopyToContainer(Transferable.of(nginxConf, 0777), "/etc/nginx/nginx.conf")
			.withCopyToContainer(Transferable.of(tls.getServerCertificatePemBytes(), 0777), "/etc/nginx/server.crt")
			.withCopyToContainer(Transferable.of(tls.getServerPrivateKeyPemBytes(), 0777), "/etc/nginx/server.key")
			.withCopyToContainer(Transferable.of(tls.getCaCertificatePemBytes(), 0777), "/etc/nginx/ca.crt");
		nginxProxy.start();
		waitForNginxReady();
	}

	protected String getEmulatorBaseUrl() {
		return "http://" + "127.0.0.1" + ":" + emulator.getMappedPort(EMULATOR_PORT);
	}

	protected String getNginxBaseUrl() {
		return "https://" + "127.0.0.1" + ":" + nginxProxy.getMappedPort(NGINX_TLS_PORT);
	}

	protected void createTopicAndSubscription() throws Exception {
		createTopic(getEmulatorBaseUrl(), PROJECT_ID, TOPIC_ID);
		createSubscription(getEmulatorBaseUrl(), PROJECT_ID, TOPIC_ID, SUBSCRIPTION_ID);
	}

	protected List<String> pullMessages() throws Exception {
		return pullMessages(getEmulatorBaseUrl(), PROJECT_ID, SUBSCRIPTION_ID);
	}

	protected void configureDestination() {
		configureDestination(Map.of());
	}

	protected void configureDestination(Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", PROJECT_ID);
		map.put("topic", TOPIC_ID);
		map.put("url", getEmulatorBaseUrl());
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", PROJECT_ID);
		map.put("topic", TOPIC_ID);
		map.put("url", getNginxBaseUrl());
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
		map.put("kind", "gcp-pubsub");
		map.put("project", PROJECT_ID);
		map.put("topic", TOPIC_ID);
		map.put("url", getNginxBaseUrl());
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

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}

	private void waitForEmulatorReady(String emulatorUrl) {
		var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(emulatorUrl))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
				client.send(request, HttpResponse.BodyHandlers.discarding());
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void waitForNginxReady() {
		var client = buildTrustAllHttpClient();
		var url = getNginxBaseUrl();
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
				client.send(request, HttpResponse.BodyHandlers.discarding());
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private static HttpClient buildTrustAllHttpClient() {
		try {
			var sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[]{new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
				public void checkClientTrusted(X509Certificate[] c, String t) {}
				public void checkServerTrusted(X509Certificate[] c, String t) {}
			}}, null);
			return HttpClient.newBuilder().sslContext(sslContext).connectTimeout(Duration.ofSeconds(5)).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createTopic(String emulatorUrl, String projectId, String topicId) throws Exception {
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder()
			.uri(URI.create(emulatorUrl + "/v1/projects/" + projectId + "/topics/" + topicId))
			.PUT(HttpRequest.BodyPublishers.ofString("{}"))
			.header("Content-Type", "application/json")
			.build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode())
			.as("Create topic response: " + response.body())
			.isIn(200, 409);
	}

	private void createSubscription(String emulatorUrl, String projectId, String topicId, String subscriptionId) throws Exception {
		var client = HttpClient.newHttpClient();
		var body = "{\"topic\":\"projects/" + projectId + "/topics/" + topicId + "\"}";
		var request = HttpRequest.newBuilder()
			.uri(URI.create(emulatorUrl + "/v1/projects/" + projectId + "/subscriptions/" + subscriptionId))
			.PUT(HttpRequest.BodyPublishers.ofString(body))
			.header("Content-Type", "application/json")
			.build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode())
			.as("Create subscription response: " + response.body())
			.isIn(200, 409);
	}

	private List<String> pullMessages(String emulatorUrl, String projectId, String subscriptionId) throws Exception {
		var client = HttpClient.newHttpClient();
		var body = "{\"maxMessages\":10}";
		var request = HttpRequest.newBuilder()
			.uri(URI.create(emulatorUrl + "/v1/projects/" + projectId + "/subscriptions/" + subscriptionId + ":pull"))
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.header("Content-Type", "application/json")
			.build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			return List.of();
		}
		var root = MAPPER.readTree(response.body());
		var receivedMessages = root.get("receivedMessages");
		if (receivedMessages == null || receivedMessages.isEmpty()) {
			return List.of();
		}
		var messages = new ArrayList<String>();
		for (var receivedMessage : receivedMessages) {
			var data = receivedMessage.get("message").get("data").asText();
			var decoded = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
			messages.add(decoded);
		}
		return messages;
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
			    server {
			        listen %d ssl;

			        ssl_certificate /etc/nginx/server.crt;
			        ssl_certificate_key /etc/nginx/server.key;
			%s
			        location / {
			            proxy_pass http://pubsub-emulator:%d;
			        }
			    }
			}
			""".formatted(NGINX_TLS_PORT, sslClientCertificate, EMULATOR_PORT);
	}
}
