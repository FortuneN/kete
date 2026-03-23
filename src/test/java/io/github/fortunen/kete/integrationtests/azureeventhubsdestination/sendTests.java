package io.github.fortunen.kete.integrationtests.azureeventhubsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Test;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startEmulator();
		configureDestination();
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read from emulator partitions to verify delivery

		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
			var body = receiveMessage();
			assertThat(body).isNotNull();
			assertThat(body).isEqualTo("{\"type\":\"LOGIN\"}");
		});
	}

	// TLS and mTLS tests are not applicable for Azure Event Hubs.
	// The Azure SDK AMQP transport manages TLS internally — EventHubClientBuilder
	// does not expose SSLContext, HttpClient, or any TLS configuration API.
	// The emulator requires UseDevelopmentEmulator=true which disables TLS.
}
