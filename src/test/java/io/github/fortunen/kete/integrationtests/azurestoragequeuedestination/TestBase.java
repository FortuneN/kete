package io.github.fortunen.kete.integrationtests.azurestoragequeuedestination;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestination;
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestinationConfig;
import okhttp3.mockwebserver.MockWebServer;

public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final String TEST_ACCOUNT_NAME = "devstoreaccount1";
	protected static final String TEST_ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

	protected MockWebServer mockServer;
	protected AzureStorageQueueDestination destination;
	protected AzureStorageQueueDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new AzureStorageQueueDestination();
		config = new AzureStorageQueueDestinationConfig();
	}

	@AfterEach
	void tearDown() throws Exception {
		cleanUpMockServer();
		if (destination != null) {
			destination.close();
		}
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
			try {
				mockServer.shutdown();
			} catch (Exception e) {
				// ignore
			}
			mockServer = null;
		}
	}

	protected void configureDestination(String queue) {
		configureDestination(queue, Map.of());
	}

	protected void configureDestination(String queue, Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", TEST_ACCOUNT_NAME);
		map.put("account-key", TEST_ACCOUNT_KEY);
		map.put("queue", queue);
		map.put("url", "http://" + mockServer.getHostName() + ":" + mockServer.getPort() + "/" + TEST_ACCOUNT_NAME);
		map.putAll(extras);
		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(String queue, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", TEST_ACCOUNT_NAME);
		map.put("account-key", TEST_ACCOUNT_KEY);
		map.put("queue", queue);
		map.put("url", "https://" + mockServer.getHostName() + ":" + mockServer.getPort() + "/" + TEST_ACCOUNT_NAME);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithMtls(String queue, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-storage-queues");
		map.put("account-name", TEST_ACCOUNT_NAME);
		map.put("account-key", TEST_ACCOUNT_KEY);
		map.put("queue", queue);
		map.put("url", "https://" + mockServer.getHostName() + ":" + mockServer.getPort() + "/" + TEST_ACCOUNT_NAME);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-path");
		map.put("tls.key-store.loader.path", tls.getKeyStoreFilePath());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}
}
