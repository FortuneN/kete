package io.github.fortunen.kete.integrationtests.socketiodestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startServer();
		configureDestination();
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — query the server's /events endpoint to verify the event was received

		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).ignoreExceptions().until(() -> !getReceivedEvents().isEmpty());

		var events = getReceivedEvents();
		assertThat(events)
			.as("Server logs: " + getContainerLogs())
			.hasSize(1);
		assertThat(events.getFirst()).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startServerWithTls(tls, false);
		configureDestinationWithTls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — query the server's plain HTTP endpoint to verify

		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).ignoreExceptions().until(() -> !getReceivedEvents().isEmpty());

		var events = getReceivedEvents();
		assertThat(events)
			.as("Server logs: " + getContainerLogs())
			.hasSize(1);
		assertThat(events.getFirst()).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startServerWithTls(tls, true);
		configureDestinationWithMtls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — query the server's plain HTTP endpoint to verify

		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).ignoreExceptions().until(() -> !getReceivedEvents().isEmpty());

		var events = getReceivedEvents();
		assertThat(events)
			.as("Server logs: " + getContainerLogs())
			.hasSize(1);
		assertThat(events.getFirst()).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
