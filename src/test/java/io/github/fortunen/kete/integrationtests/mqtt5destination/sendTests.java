package io.github.fortunen.kete.integrationtests.mqtt5destination;

import static org.assertj.core.api.Assertions.assertThat;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startMqtt();
		var topic = "test/events";
		var map = new HashMap<String, Object>();
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(container.getMqttPort()));
		map.put("topic", topic);
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		try (var subscriber = createSubscriber(topic, 1, (t, msg) -> {
			receivedMessage.set(msg.getPayload());
		})) {
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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
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
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithServerOnlyTLS(tls);
		var topic = "test/events";
		var map = new HashMap<String, Object>();
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(getMqttTlsPort()));
		map.put("topic", topic);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		try (var subscriber = createSubscriberWithTls(topic, 1, tls, (t, msg) -> {
			receivedMessage.set(msg.getPayload());
		})) {
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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
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
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithClientAndServerTLS(tls);
		var topic = "test/events";
		var map = new HashMap<String, Object>();
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(getMqttTlsPort()));
		map.put("topic", topic);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-base64");
		map.put("tls.trust-store.loader.base64", tls.getTrustStoreBase64());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-base64");
		map.put("tls.key-store.loader.base64", tls.getKeyStoreBase64());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		var receivedMessage = new AtomicReference<byte[]>();

		try (var subscriber = createSubscriberWithTls(topic, 1, tls, (t, msg) -> {
			receivedMessage.set(msg.getPayload());
		})) {
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

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> receivedMessage.get() != null);
			assertThat(new String(receivedMessage.get(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}
}
