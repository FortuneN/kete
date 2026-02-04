package io.github.fortunen.kete.integrationtests.pulsardestination;

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

		startPulsar();
		var topic = "persistent://public/default/test-events";
		var map = new HashMap<String, Object>();
		map.put("service-url", getPulsarUrl());
		map.put("topic", topic);
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		try (var subscriber = createSubscriber(topic)) {

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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
				try {
					var messages = receiveMessages(subscriber, 1, 1);
					if (!messages.isEmpty()) {
						var received = new String(messages.get(0), StandardCharsets.UTF_8);
						return received.contains("{\"type\":\"LOGIN\"}");
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			});
		}
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1" })
			.build();

		startWithServerOnlyTLS(tls);

		var topic = "persistent://public/default/test-events";
		var map = new HashMap<String, Object>();
		map.put("service-url", getPulsarTlsUrl());
		map.put("topic", topic);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		try (var subscriber = createSubscriberWithTls(topic, tls)) {

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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
				try {
					var messages = receiveMessages(subscriber, 1, 1);
					if (!messages.isEmpty()) {
						var received = new String(messages.get(0), StandardCharsets.UTF_8);
						return received.contains("{\"type\":\"LOGIN\"}");
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			});
		}
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withWriteFiles(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1" })
			.build();

		startWithClientAndServerTLS(tls);

		var topic = "persistent://public/default/test-events";
		var map = new HashMap<String, Object>();
		map.put("service-url", getPulsarTlsUrl());
		map.put("topic", topic);
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

		try (var subscriber = createSubscriberWithTls(topic, tls)) {

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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
				try {
					var messages = receiveMessages(subscriber, 1, 1);
					if (!messages.isEmpty()) {
						var received = new String(messages.get(0), StandardCharsets.UTF_8);
						return received.contains("{\"type\":\"LOGIN\"}");
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			});
		}
	}
}
