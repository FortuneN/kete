package io.github.fortunen.kete.integrationtests.redispubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startRedis();
		var map = new HashMap<String, Object>();
		map.put("host", getHost());
		map.put("port", getPort());
		map.put("channel", "test-channel");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriber("test-channel", collector)) {

			Thread.sleep(500);

			var message = createMessage(
				"test-event-id",
				"test-realm",
				false,
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
				null,
				null
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> !collector.getMessages().isEmpty());

			assertThat(collector.getMessages()).hasSize(1);
			assertThat(collector.getMessages().get(0)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal" })
			.build();

		startWithServerOnlyTLS(tls);
		var map = new HashMap<String, Object>();
		map.put("host", getHost());
		map.put("port", getTlsPort());
		map.put("channel", "test-channel");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriberWithTls("test-channel", collector, tls)) {

			Thread.sleep(500);

			var message = createMessage(
				"test-event-id",
				"test-realm",
				false,
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
				null,
				null
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> !collector.getMessages().isEmpty());

			assertThat(collector.getMessages()).hasSize(1);
			assertThat(collector.getMessages().get(0)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal" })
			.build();

		startWithClientAndServerTLS(tls);
		var map = new HashMap<String, Object>();
		map.put("host", getHost());
		map.put("port", getTlsPort());
		map.put("channel", "test-channel");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-base64");
		map.put("tls.trust-store.loader.base64", tls.getTrustStoreBase64());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-base64");
		map.put("tls.key-store.loader.base64", tls.getKeyStoreBase64());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-password", tls.getKeyPassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var collector = new MessageCollector();
		try (var subscriber = createSubscriberWithTls("test-channel", collector, tls)) {

			Thread.sleep(500);

			var message = createMessage(
				"test-event-id",
				"test-realm",
				false,
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8),
				null,
				null
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> !collector.getMessages().isEmpty());

			assertThat(collector.getMessages()).hasSize(1);
			assertThat(collector.getMessages().get(0)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}
}
