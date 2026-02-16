package io.github.fortunen.kete.integrationtests.azureeventgriddestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import okhttp3.mockwebserver.MockResponse;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange — Event Grid SDK requires HTTPS, so use MockWebServer in HTTPS mode

		var tls = TlsMaterial.builder()
			.withEnabled(true)
			.withTrustStorePassword("changeit")
			.withKeyStorePassword("changeit")
			.withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startMockServerWithTls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for initialize() test GET
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for send() POST
		configureDestinationWithTls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // discard initialize() test GET
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getBody().readUtf8()).contains("\"type\":\"LOGIN\"");
		assertThat(request.getHeader("aeg-sas-key")).isEqualTo(ACCESS_KEY);
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
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for initialize() test GET
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for send() POST
		configureDestinationWithTls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // discard initialize() test GET
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getBody().readUtf8()).contains("\"type\":\"LOGIN\"");
		assertThat(request.getHeader("aeg-sas-key")).isEqualTo(ACCESS_KEY);
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

		startMockServerWithTls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for initialize() test GET
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for send() POST
		configureDestinationWithMtls(tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // discard initialize() test GET
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getBody().readUtf8()).contains("\"type\":\"LOGIN\"");
		assertThat(request.getHeader("aeg-sas-key")).isEqualTo(ACCESS_KEY);
	}
}
