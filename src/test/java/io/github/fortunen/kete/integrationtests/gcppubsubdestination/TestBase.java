package io.github.fortunen.kete.integrationtests.gcppubsubdestination;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.gcppubsub.GcpPubSubDestination;
import io.github.fortunen.kete.destinations.gcppubsub.GcpPubSubDestinationConfig;
import okhttp3.mockwebserver.MockWebServer;

public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];

	protected MockWebServer mockServer;
	protected GcpPubSubDestination destination;
	protected GcpPubSubDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new GcpPubSubDestination();
		config = new GcpPubSubDestinationConfig();
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

	protected void configureDestination(String project, String topic) {
		configureDestination(project, topic, Map.of());
	}

	protected void configureDestination(String project, String topic, Map<String, Object> extras) {

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", project);
		map.put("topic", topic);
		map.put("url", "http://" + mockServer.getHostName() + ":" + mockServer.getPort());
		map.putAll(extras);

		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(String project, String topic, TlsMaterial tls) {

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", project);
		map.put("topic", topic);
		map.put("url", "https://" + mockServer.getHostName() + ":" + mockServer.getPort());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());

		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithMtls(String project, String topic, TlsMaterial tls) {

		var map = new HashMap<String, Object>();
		map.put("kind", "gcp-pubsub");
		map.put("project", project);
		map.put("topic", topic);
		map.put("url", "https://" + mockServer.getHostName() + ":" + mockServer.getPort());
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
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "false", "", "");
	}
}
