package io.github.fortunen.kete.integrationtests.awssqsdestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startLocalStack();
		createQueue("my-queue");
		configureDestination("my-queue");
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read from LocalStack SQS to verify the message was sent

		var body = receiveMessage("my-queue");
		assertThat(body).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startLocalStackWithTls(tls);
		createQueue("my-queue");
		configureDestinationWithTls("my-queue", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read directly from LocalStack (plain HTTP) to verify

		var body = receiveMessage("my-queue");
		assertThat(body).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startLocalStackWithTls(tls);
		createQueue("my-queue");
		configureDestinationWithMtls("my-queue", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read directly from LocalStack (plain HTTP) to verify

		var body = receiveMessage("my-queue");
		assertThat(body).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
