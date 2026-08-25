package io.github.fortunen.kete.integrationtests.azurewebpubsubdestination;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.azurewebpubsub.AzureWebPubSubDestination;
import io.github.fortunen.kete.destinations.azurewebpubsub.AzureWebPubSubDestinationConfig;
import okhttp3.mockwebserver.MockWebServer;

public class TestBase {

	private static final String ACCESS_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

	protected static final String HUB_NAME = "test_hub";

	protected MockWebServer mockServer;
	protected AzureWebPubSubDestination destination;
	protected AzureWebPubSubDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new AzureWebPubSubDestination();
		config = new AzureWebPubSubDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		cleanUpMockServer();
	}

	protected MockWebServer startMockServer() throws Exception {
		cleanUpMockServer();
		mockServer = new MockWebServer();
		mockServer.start();
		return mockServer;
	}

	protected MockWebServer startMockServerWithTls(TlsMaterial tls) throws Exception {
		cleanUpMockServer();
		mockServer = new MockWebServer();
		mockServer.useHttps(tls.getServerKeyStoreSSLContext().getSocketFactory(), false);
		mockServer.start();
		return mockServer;
	}

	protected MockWebServer startMockServerWithMtls(TlsMaterial tls) throws Exception {
		cleanUpMockServer();
		mockServer = new MockWebServer();
		mockServer.useHttps(tls.getServerKeyStoreSSLContext().getSocketFactory(), false);
		mockServer.requireClientAuth();
		mockServer.start();
		return mockServer;
	}

	protected void cleanUpMockServer() {
		if (mockServer != null) {
			try { mockServer.shutdown(); } catch (Exception e) { /* ignore */ }
			mockServer = null;
		}
	}

	protected String getConnectionString() {
		return "Endpoint=http://" + mockServer.getHostName() + ":" + mockServer.getPort() + ";AccessKey=" + ACCESS_KEY + ";Version=1.0;";
	}

	protected String getTlsConnectionString() {
		return "Endpoint=https://" + mockServer.getHostName() + ":" + mockServer.getPort() + ";AccessKey=" + ACCESS_KEY + ";Version=1.0;";
	}

	protected void configureDestination() {
		configureDestination(Map.of());
	}

	protected void configureDestination(Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-webpubsub");
		map.put("connection-string", getConnectionString());
		map.put("hub", HUB_NAME);
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-webpubsub");
		map.put("connection-string", getTlsConnectionString());
		map.put("hub", HUB_NAME);
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
		map.put("kind", "azure-webpubsub");
		map.put("connection-string", getTlsConnectionString());
		map.put("hub", HUB_NAME);
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
}
