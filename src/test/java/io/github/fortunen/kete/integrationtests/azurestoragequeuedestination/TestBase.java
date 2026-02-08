package io.github.fortunen.kete.integrationtests.azurestoragequeuedestination;

import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

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
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestination;
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestinationConfig;
import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

public class TestBase {

	private static final int QUEUE_PORT = 10001;
	private static final int NGINX_TLS_PORT = 8443;
	private static final String API_VERSION = "2024-08-04";
	private static final String NGINX_IMAGE = "nginx:1.27-alpine";
	protected static final String ACCOUNT_NAME = "devstoreaccount1";
	private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";
	protected static final String ACCOUNT_KEY = AzureStorageQueueUtils.WELL_KNOWN_ACCOUNT_KEY;
	private static final DateTimeFormatter RFC_1123_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

	protected Network network;
	private SecretKeySpec testKeySpec;
	protected GenericContainer<?> azurite;
	protected GenericContainer<?> nginxProxy;
	protected AzureStorageQueueDestination destination;
	protected AzureStorageQueueDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new AzureStorageQueueDestination();
		config = new AzureStorageQueueDestinationConfig();
		testKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(ACCOUNT_KEY);
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
		if (azurite != null) {
			try { azurite.stop(); } catch (Exception e) { /* ignore */ }
			azurite = null;
		}
		if (network != null) {
			try { network.close(); } catch (Exception e) { /* ignore */ }
			network = null;
		}
	}

	protected void startAzurite() {
		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withExposedPorts(QUEUE_PORT)
			.withCommand("azurite-queue", "--queueHost", "0.0.0.0", "--queuePort", String.valueOf(QUEUE_PORT))
			.waitingFor(Wait.forListeningPort())
			.withStartupTimeout(Duration.ofMinutes(2));
		azurite.start();
		waitForAzuriteReady();
	}

	protected void startAzuriteOnNetwork() {
		network = Network.newNetwork();
		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withNetwork(network)
			.withNetworkAliases("azurite")
			.withExposedPorts(QUEUE_PORT)
			.withCommand("azurite-queue", "--queueHost", "0.0.0.0", "--queuePort", String.valueOf(QUEUE_PORT))
			.waitingFor(Wait.forListeningPort())
			.withStartupTimeout(Duration.ofMinutes(2));
		azurite.start();
		waitForAzuriteReady();
	}

	protected void startNginxTlsProxy(TlsMaterial tls, boolean requireClientAuth) throws Exception {
		var nginxConf = createNginxConfig(requireClientAuth);
		nginxProxy = new GenericContainer<>(DockerImageName.parse(NGINX_IMAGE))
			.withNetwork(network)
			.withExposedPorts(NGINX_TLS_PORT)
			.withCopyToContainer(Transferable.of(nginxConf, 0777), "/etc/nginx/nginx.conf")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getServerCertificatePemFilePath())), 0777), "/etc/nginx/server.crt")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getServerPrivateKeyPemFilePath())), 0777), "/etc/nginx/server.key")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getCaCertificatePemFilePath())), 0777), "/etc/nginx/ca.crt")
			.waitingFor(Wait.forListeningPort())
			.withStartupTimeout(Duration.ofMinutes(2));
		nginxProxy.start();
	}

	protected String getAzuriteBaseUrl() {
		return "http://" + azurite.getHost() + ":" + azurite.getMappedPort(QUEUE_PORT) + "/" + ACCOUNT_NAME;
	}

	protected String getNginxBaseUrl() {
		return "https://" + nginxProxy.getHost() + ":" + nginxProxy.getMappedPort(NGINX_TLS_PORT) + "/" + ACCOUNT_NAME;
	}

	protected void createQueue(String queue) throws Exception {
		var url = getAzuriteBaseUrl() + "/" + queue;
		var request = authenticatedRequest("PUT", url, queue)
			.PUT(HttpRequest.BodyPublishers.noBody())
			.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 201 && response.statusCode() != 204) {
			throw new RuntimeException("Failed to create queue '" + queue + "': HTTP " + response.statusCode() + " " + response.body());
		}
	}

	protected String peekMessage(String queue) throws Exception {
		var url = getAzuriteBaseUrl() + "/" + queue + "/messages?peekonly=true";
		var request = authenticatedRequest("GET", url, queue + "/messages")
			.GET()
			.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	protected void configureDestination(String queue) {
		configureDestination(queue, Map.of());
	}

	protected void configureDestination(String queue, Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("account-name", ACCOUNT_NAME);
		map.put("account-key", ACCOUNT_KEY);
		map.put("queue", queue);
		map.put("url", getAzuriteBaseUrl());
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(String queue, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("account-name", ACCOUNT_NAME);
		map.put("account-key", ACCOUNT_KEY);
		map.put("queue", queue);
		map.put("url", getNginxBaseUrl());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithMtls(String queue, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("account-name", ACCOUNT_NAME);
		map.put("account-key", ACCOUNT_KEY);
		map.put("queue", queue);
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

	protected String extractMessageText(String xml) {
		var start = xml.indexOf("<MessageText>") + "<MessageText>".length();
		var end = xml.indexOf("</MessageText>");
		return xml.substring(start, end);
	}

	private void waitForAzuriteReady() {
		var baseUrl = getAzuriteBaseUrl();
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
				HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private HttpRequest.Builder authenticatedRequest(String verb, String url, String canonicalResource) {
		var date = ZonedDateTime.now(ZoneOffset.UTC).format(RFC_1123_FORMATTER);
		var stringToSign = verb + "\n\n\n\n\n\n\n\n\n\n\n\n"
			+ "x-ms-date:" + date + "\n"
			+ "x-ms-version:" + API_VERSION + "\n"
			+ "/" + ACCOUNT_NAME + "/" + canonicalResource;
		var signature = AzureStorageQueueUtils.computeSignature(testKeySpec, stringToSign);
		return HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("x-ms-date", date)
			.header("x-ms-version", API_VERSION)
			.header("Authorization", "SharedKey " + ACCOUNT_NAME + ":" + signature);
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
			            proxy_pass http://azurite:%d;
			        }
			    }
			}
			""".formatted(NGINX_TLS_PORT, sslClientCertificate, QUEUE_PORT);
	}
}
