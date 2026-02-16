package io.github.fortunen.kete.integrationtests.azureeventgriddestination;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.azureeventgrid.AzureEventGridDestination;
import io.github.fortunen.kete.destinations.azureeventgrid.AzureEventGridDestinationConfig;
import okhttp3.mockwebserver.MockWebServer;

public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final String ACCESS_KEY = "test-access-key-for-integration-tests";

	protected MockWebServer mockServer;
	protected AzureEventGridDestination destination;
	protected AzureEventGridDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new AzureEventGridDestination();
		config = new AzureEventGridDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		cleanUpMockServer();
	}

	protected MockWebServer startMockServerWithTls(TlsMaterial tls) throws Exception {
		cleanUpMockServer();
		mockServer = new MockWebServer();
		mockServer.useHttps(tls.getServerKeyStoreSSLContext().getSocketFactory(), false);
		mockServer.start();
		return mockServer;
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventgrid");
		map.put("endpoint", "https://" + mockServer.getHostName() + ":" + mockServer.getPort());
		map.put("access-key", ACCESS_KEY);
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
		map.put("kind", "azure-eventgrid");
		map.put("endpoint", "https://" + mockServer.getHostName() + ":" + mockServer.getPort());
		map.put("access-key", ACCESS_KEY);
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
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "false", "", "");
	}

	private void cleanUpMockServer() {
		if (mockServer != null) {
			try { mockServer.shutdown(); } catch (Exception e) { /* ignore */ }
			mockServer = null;
		}
	}
}
