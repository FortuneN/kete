package io.github.fortunen.kete.integrationtests.gcpcloudtasksdestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startTaskReceiver();
		startEmulator();
		configureDestination();
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — verify the emulator dispatched the task to our HTTP receiver

		var dispatchedRequest = waitForDispatchedTask();
		assertThat(dispatchedRequest).isNotNull();
		assertThat(dispatchedRequest.getMethod()).isEqualTo("POST");
		assertThat(dispatchedRequest.getBody().readUtf8()).isEqualTo("{\"type\":\"LOGIN\"}");
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

		startTaskReceiver();
		startEmulatorOnNetwork();
		startNginxTlsProxy(tls, false);
		configureDestinationWithTls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — verify the emulator dispatched the task to our HTTP receiver

		var dispatchedRequest = waitForDispatchedTask();
		assertThat(dispatchedRequest).isNotNull();
		assertThat(dispatchedRequest.getMethod()).isEqualTo("POST");
		assertThat(dispatchedRequest.getBody().readUtf8()).isEqualTo("{\"type\":\"LOGIN\"}");
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

		startTaskReceiver();
		startEmulatorOnNetwork();
		startNginxTlsProxy(tls, true);
		configureDestinationWithMtls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — verify the emulator dispatched the task to our HTTP receiver

		var dispatchedRequest = waitForDispatchedTask();
		assertThat(dispatchedRequest).isNotNull();
		assertThat(dispatchedRequest.getMethod()).isEqualTo("POST");
		assertThat(dispatchedRequest.getBody().readUtf8()).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
