package io.github.fortunen.kete.integrationtests.websocketdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startWebSocketServer();
		var map = new HashMap<String, Object>();
		map.put("host", "localhost");
		map.put("port", String.valueOf(getWebSocketPort()));
		map.put("path", "/");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).until(() -> !receivedMessages.isEmpty());

		var received = receivedMessages.poll(1, TimeUnit.SECONDS);
		assertThat(received).isNotNull();
		assertThat(received).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange - Start host-side WebSocket server with nginx TLS termination

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
		map.put("path", "/");
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

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).until(() -> !receivedMessages.isEmpty());

		var received = receivedMessages.poll(1, TimeUnit.SECONDS);
		assertThat(received).isNotNull();
		assertThat(received).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange - Start host-side WebSocket server with nginx mTLS (client auth required)

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
		map.put("path", "/");
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

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).until(() -> !receivedMessages.isEmpty());

		var received = receivedMessages.poll(1, TimeUnit.SECONDS);
		assertThat(received).isNotNull();
		assertThat(received).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
