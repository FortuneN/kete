package io.github.fortunen.kete.integrationtests.azurewebpubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import okhttp3.mockwebserver.MockResponse;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startMockServer();
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // connection test response
		mockServer.enqueue(new MockResponse().setResponseCode(202)); // sendToAll response
		configureDestination();
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — verify the REST API request sent to MockWebServer

		mockServer.takeRequest(1, TimeUnit.MINUTES); // skip connection test GET
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).contains("/api/hubs/" + HUB_NAME);
		assertThat(request.getBody().readUtf8()).isEqualTo("{\"type\":\"LOGIN\"}");
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

		startMockServerWithTls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // connection test response
		mockServer.enqueue(new MockResponse().setResponseCode(202)); // sendToAll response
		configureDestinationWithTls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // skip connection test GET
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).contains("/api/hubs/" + HUB_NAME);
		assertThat(request.getBody().readUtf8()).isEqualTo("{\"type\":\"LOGIN\"}");
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

		startMockServerWithMtls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // connection test response
		mockServer.enqueue(new MockResponse().setResponseCode(202)); // sendToAll response
		configureDestinationWithMtls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // skip connection test GET
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).contains("/api/hubs/" + HUB_NAME);
		assertThat(request.getBody().readUtf8()).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
