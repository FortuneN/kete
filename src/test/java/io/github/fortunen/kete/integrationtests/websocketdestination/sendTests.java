package io.github.fortunen.kete.integrationtests.websocketdestination;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startWebSocketEchoServer();
		var map = new HashMap<String, Object>();
		map.put("host", container.getHost());
		map.put("port", String.valueOf(getWebSocketPort()));
		map.put("path", "/.ws");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act & assert - echo server accepts and echoes, no exception means success

		assertThatCode(() -> destination.send(message)).doesNotThrowAnyException();
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange - Start WebSocket echo server with nginx TLS termination

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1" })
			.build();

		startWithServerOnlyTLS(tls);

		var map = new HashMap<String, Object>();
		map.put("host", getNginxHost());
		map.put("port", String.valueOf(getWebSocketTlsPort()));
		map.put("path", "/.ws");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act & assert - send over TLS connection

		assertThatCode(() -> destination.send(message)).doesNotThrowAnyException();
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange - Start WebSocket echo server with nginx mTLS (client auth required)

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1" })
			.build();

		startWithClientAndServerTLS(tls);

		var map = new HashMap<String, Object>();
		map.put("host", getNginxHost());
		map.put("port", String.valueOf(getWebSocketTlsPort()));
		map.put("path", "/.ws");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-path");
		map.put("tls.key-store.loader.path", tls.getKeyStoreFilePath());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act & assert - send over mTLS connection

		assertThatCode(() -> destination.send(message)).doesNotThrowAnyException();
	}
}
