package io.github.fortunen.kete.integrationtests.awskinesisdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

@Tag("IntegrationTest")
class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		var stream = "test-stream-nontls";
		startLocalStack();
		createStream(stream);
		configureDestination(stream);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
			var record = readRecordFromStream(stream);
			assertThat(record).isEqualTo("{\"type\":\"LOGIN\"}");
		});
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var stream = "test-stream-tls";
		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startLocalStackWithTls(tls);
		createStream(stream);
		configureDestinationWithTls(stream, tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN_TLS\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
			var record = readRecordFromStream(stream);
			assertThat(record).isEqualTo("{\"type\":\"LOGIN_TLS\"}");
		});
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var stream = "test-stream-mtls";
		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startLocalStackWithTls(tls);
		createStream(stream);
		configureDestinationWithMtls(stream, tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN_MTLS\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
			var record = readRecordFromStream(stream);
			assertThat(record).isEqualTo("{\"type\":\"LOGIN_MTLS\"}");
		});
	}
}
