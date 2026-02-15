package io.github.fortunen.kete.integrationtests.grpcdestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startGrpcServer();
		configureDestination();
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		assertThat(receivedPayloads).hasSize(2); // 1 verify + 1 send
		var payload = new String(receivedPayloads.get(1), StandardCharsets.UTF_8);
		assertThat(payload).isEqualTo("{\"type\":\"LOGIN\"}");
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

		startGrpcServer();
		startNginxTlsProxy(tls, false);
		configureDestinationWithTls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		assertThat(receivedPayloads).hasSize(2); // 1 verify + 1 send
		var payload = new String(receivedPayloads.get(1), StandardCharsets.UTF_8);
		assertThat(payload).isEqualTo("{\"type\":\"LOGIN\"}");
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

		startGrpcServer();
		startNginxTlsProxy(tls, true);
		configureDestinationWithMtls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		assertThat(receivedPayloads).hasSize(2); // 1 verify + 1 send
		var payload = new String(receivedPayloads.get(1), StandardCharsets.UTF_8);
		assertThat(payload).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
