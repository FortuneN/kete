package io.github.fortunen.kete.integrationtests.stompdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startActiveMq();
		var stompDestination = "/queue/test-events";
		var map = new HashMap<String, Object>();
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(getStompPort()));
		map.put("destination", stompDestination);
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		try (var subscriber = createSubscriber(stompDestination, 1)) {

			var receivedMessage = new AtomicReference<String>();

			var message = createMessage(
				"test-event-id",
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
				try {
					var received = subscriber.takeMessage(1, TimeUnit.SECONDS);
					if (received != null) {
						receivedMessage.set(received);
						return true;
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			});

			assertThat(receivedMessage.get()).isNotNull();
			assertThat(receivedMessage.get()).isEqualTo("{\"type\":\"LOGIN\"}");
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
		var stompDestination = "/queue/test-events";
		var map = new HashMap<String, Object>();
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(getStompsPort()));
		map.put("destination", stompDestination);
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		try (var subscriber = createTlsSubscriber(stompDestination, tls)) {

			var receivedMessage = new AtomicReference<String>();

			var message = createMessage(
				"test-event-id",
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
				try {
					var received = subscriber.takeMessage(1, TimeUnit.SECONDS);
					if (received != null) {
						receivedMessage.set(received);
						return true;
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			});

			assertThat(receivedMessage.get()).isNotNull();
			assertThat(receivedMessage.get()).isEqualTo("{\"type\":\"LOGIN\"}");
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
		var stompDestination = "/queue/test-events";
		var map = new HashMap<String, Object>();
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(getStompsPort()));
		map.put("destination", stompDestination);
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

		try (var subscriber = createTlsSubscriber(stompDestination, tls)) {

			var receivedMessage = new AtomicReference<String>();

			var message = createMessage(
				"test-event-id",
				"LOGIN",
				"application/json",
				"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
			);

			// act

			destination.send(message);

			// assert

			await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
				try {
					var received = subscriber.takeMessage(1, TimeUnit.SECONDS);
					if (received != null) {
						receivedMessage.set(received);
						return true;
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			});

			assertThat(receivedMessage.get()).isNotNull();
			assertThat(receivedMessage.get()).isEqualTo("{\"type\":\"LOGIN\"}");
		}
	}
}
