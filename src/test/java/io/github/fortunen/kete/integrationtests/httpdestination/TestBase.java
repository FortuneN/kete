package io.github.fortunen.kete.integrationtests.httpdestination;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.http.HttpDestination;
import io.github.fortunen.kete.destinations.http.HttpDestinationConfig;
import okhttp3.mockwebserver.MockWebServer;

public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];

	protected MockWebServer mockServer;
	protected HttpDestination destination;
	protected HttpDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new HttpDestination();
		config = new HttpDestinationConfig();
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
		// requireClientAuth() enforces mTLS - server REQUIRES valid client certificate
		// Without a valid client cert, the TLS handshake will fail
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

	protected void configureDestinationWithMockServer() {
		var map = new HashMap<String, Object>();
		map.put("kind", "http");
		map.put("host", mockServer.getHostName());
		map.put("port", mockServer.getPort());
		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "false", "", "");
	}
}
