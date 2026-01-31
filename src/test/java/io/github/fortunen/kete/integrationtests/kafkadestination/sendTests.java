package io.github.fortunen.kete.integrationtests.kafkadestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startKafka();
		var map = new HashMap<String, Object>();
		map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
		map.put("topic", "test-topic");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

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

		try (var consumer = createConsumer("test-topic")) {
			var records = consumer.poll(Duration.ofSeconds(10));
			assertThat(records.count()).isEqualTo(1);

			var record = records.iterator().next();
			assertThat(record.topic()).isEqualTo("test-topic");
			assertThat(new String(record.value(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
			assertThat(record.key()).isEqualTo("LOGIN");
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
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithServerOnlyTLS(tls);
		var map = new HashMap<String, Object>();
		map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getSslBootstrapServers());
		map.put("topic", "test-topic");
		map.put("security.protocol", "SSL");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

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

		try (var consumer = createSslConsumer("test-topic", tls)) {
			var records = consumer.poll(Duration.ofSeconds(10));
			assertThat(records.count()).isEqualTo(1);

			var record = records.iterator().next();
			assertThat(record.topic()).isEqualTo("test-topic");
			assertThat(new String(record.value(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
			assertThat(record.key()).isEqualTo("LOGIN");
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
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startWithClientAndServerTLS(tls);
		var map = new HashMap<String, Object>();
		map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getSslBootstrapServers());
		map.put("topic", "test-topic");
		map.put("security.protocol", "SSL");
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

		try (var consumer = createSslConsumer("test-topic", tls)) {
			var records = consumer.poll(Duration.ofSeconds(10));
			assertThat(records.count()).isEqualTo(1);

			var record = records.iterator().next();
			assertThat(record.topic()).isEqualTo("test-topic");
			assertThat(new String(record.value(), StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"LOGIN\"}");
			assertThat(record.key()).isEqualTo("LOGIN");
		}
	}
}
