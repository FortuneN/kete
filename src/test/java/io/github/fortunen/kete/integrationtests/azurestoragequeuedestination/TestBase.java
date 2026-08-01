package io.github.fortunen.kete.integrationtests.azurestoragequeuedestination;

import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.QueueMessageEncoding;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestination;
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestinationConfig;
import io.github.fortunen.kete.utils.AzureUtils;
public class TestBase {

	private static final int QUEUE_PORT = 10001;
	protected static final String ACCOUNT_NAME = "devstoreaccount1";
	private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite:3.36.0";
	protected static final String ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

	protected GenericContainer<?> azurite;
	protected AzureStorageQueueDestination destination;
	protected AzureStorageQueueDestinationConfig config;
	private TlsMaterial currentTls;

	@BeforeEach
	void setUp() {
		destination = new AzureStorageQueueDestination();
		config = new AzureStorageQueueDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (azurite != null) {
			try { azurite.stop(); } catch (Exception e) { /* ignore */ }
			azurite = null;
		}
		currentTls = null;
	}

	@SuppressWarnings("resource")
	protected void startAzurite() {
		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withExposedPorts(QUEUE_PORT)
			.withCommand("azurite-queue", "--queueHost", "0.0.0.0", "--queuePort", String.valueOf(QUEUE_PORT));
		azurite.start();
		waitForAzuriteReady();
	}

	@SuppressWarnings("resource")
	protected void startAzuriteWithTls(TlsMaterial tls) {
		currentTls = tls;
		var certChain = new String(tls.getServerCertificatePemBytes(), StandardCharsets.UTF_8)
			+ new String(tls.getCaCertificatePemBytes(), StandardCharsets.UTF_8);
		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withExposedPorts(QUEUE_PORT)
			.withCommand("azurite-queue", "--queueHost", "0.0.0.0", "--queuePort", String.valueOf(QUEUE_PORT),
				"--cert", "/certs/server.pem", "--key", "/certs/server.key")
			.withCopyToContainer(Transferable.of(certChain.getBytes(StandardCharsets.UTF_8), 0777), "/certs/server.pem")
			.withCopyToContainer(Transferable.of(tls.getServerPrivateKeyPemBytes(), 0777), "/certs/server.key");
		azurite.start();
		waitForAzuriteReady();
	}

	protected String getAzuriteBaseUrl() {
		var protocol = currentTls != null ? "https" : "http";
		return protocol + "://" + "127.0.0.1" + ":" + azurite.getMappedPort(QUEUE_PORT) + "/" + ACCOUNT_NAME;
	}

	protected void createQueue(String queue) {
		var builder = new QueueClientBuilder()
			.connectionString(buildAzuriteConnectionString())
			.queueName(queue);
		if (currentTls != null) {
			builder.httpClient(AzureUtils.createHttpClient(currentTls, Duration.ofSeconds(30)));
		}
		builder.buildClient().create();
	}

	protected String peekMessage(String queue) {
		var builder = new QueueClientBuilder()
			.connectionString(buildAzuriteConnectionString())
			.queueName(queue)
			.messageEncoding(QueueMessageEncoding.BASE64);
		if (currentTls != null) {
			builder.httpClient(AzureUtils.createHttpClient(currentTls, Duration.ofSeconds(30)));
		}
		return builder.buildClient().peekMessage().getBody().toString();
	}

	protected void configureDestination(String queue) {
		configureDestination(queue, Map.of());
	}

	protected void configureDestination(String queue, Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", buildAzuriteConnectionString());
		map.put("queue", queue);
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(String queue, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queue");
		map.put("connection-string", buildAzuriteConnectionString());
		map.put("queue", queue);
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
		map.put("connection-string", buildAzuriteConnectionString());
		map.put("queue", queue);
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

	protected String buildAzuriteConnectionString() {
		var protocol = currentTls != null ? "https" : "http";
		return "DefaultEndpointsProtocol=" + protocol + ";AccountName=" + ACCOUNT_NAME + ";AccountKey=" + ACCOUNT_KEY + ";QueueEndpoint=" + getAzuriteBaseUrl();
	}

	private void waitForAzuriteReady() {
		var client = currentTls != null ? buildTrustAllHttpClient() : HttpClient.newHttpClient();
		var baseUrl = getAzuriteBaseUrl();
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl))
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
}
