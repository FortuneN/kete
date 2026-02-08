package io.github.fortunen.kete.integrationtests.gcppubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import okhttp3.mockwebserver.MockResponse;

public class sendTests extends TestBase {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startMockServer();
		mockServer.enqueue(new MockResponse().setResponseCode(200));
		mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"messageIds\":[\"1\"]}"));

		configureDestination("my-project", "my-topic");
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

		mockServer.takeRequest(); // discard initialize test request
		var request = mockServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).isEqualTo("/v1/projects/my-project/topics/my-topic:publish");
		assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
		var body = MAPPER.readTree(request.getBody().readUtf8());
		var messageNode = body.get("messages").get(0);
		var decoded = new String(Base64.getDecoder().decode(messageNode.get("data").asText()), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo("{\"type\":\"LOGIN\"}");
		assertThat(messageNode.get("attributes").get("eventtype").asText()).isEqualTo("LOGIN");
		assertThat(messageNode.get("attributes").get("contenttype").asText()).isEqualTo("application/json");
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

		startMockServerWithTls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(200));
		mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"messageIds\":[\"1\"]}"));

		configureDestinationWithTls("my-project", "my-topic", tls);
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

		mockServer.takeRequest(); // discard initialize test request
		var request = mockServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).isEqualTo("/v1/projects/my-project/topics/my-topic:publish");
		var body = MAPPER.readTree(request.getBody().readUtf8());
		var decoded = new String(Base64.getDecoder().decode(body.get("messages").get(0).get("data").asText()), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo("{\"type\":\"LOGIN\"}");
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

		startMockServerWithMtls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(200));
		mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"messageIds\":[\"1\"]}"));

		configureDestinationWithMtls("my-project", "my-topic", tls);
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

		mockServer.takeRequest(); // discard initialize test request
		var request = mockServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).isEqualTo("/v1/projects/my-project/topics/my-topic:publish");
		var body = MAPPER.readTree(request.getBody().readUtf8());
		var decoded = new String(Base64.getDecoder().decode(body.get("messages").get(0).get("data").asText()), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
