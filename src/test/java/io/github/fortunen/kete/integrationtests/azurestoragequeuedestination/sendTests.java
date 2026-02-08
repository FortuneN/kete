package io.github.fortunen.kete.integrationtests.azurestoragequeuedestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import okhttp3.mockwebserver.MockResponse;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startMockServer();
		mockServer.enqueue(new MockResponse().setResponseCode(201).setBody("<?xml version=\"1.0\"?><QueueMessagesList><QueueMessage><MessageId>id</MessageId></QueueMessage></QueueMessagesList>"));

		configureDestination("my-queue");
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		var request = mockServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).isEqualTo("/devstoreaccount1/my-queue/messages");
		assertThat(request.getHeader("Content-Type")).isEqualTo("application/xml");
		assertThat(request.getHeader("Authorization")).startsWith("SharedKey devstoreaccount1:");
		assertThat(request.getHeader("x-ms-date")).isNotBlank();
		assertThat(request.getHeader("x-ms-version")).isEqualTo("2024-08-04");

		var body = request.getBody().readUtf8();
		assertThat(body).startsWith("<QueueMessage><MessageText>");
		assertThat(body).endsWith("</MessageText></QueueMessage>");

		var base64Content = body.replace("<QueueMessage><MessageText>", "").replace("</MessageText></QueueMessage>", "");
		var decoded = new String(Base64.getDecoder().decode(base64Content), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_Tls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true).withWriteFiles(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startMockServerWithTls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(201).setBody("<?xml version=\"1.0\"?><QueueMessagesList><QueueMessage><MessageId>id</MessageId></QueueMessage></QueueMessagesList>"));

		configureDestinationWithTls("my-queue", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		var request = mockServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).isEqualTo("/devstoreaccount1/my-queue/messages");
		assertThat(request.getHeader("Authorization")).startsWith("SharedKey devstoreaccount1:");

		var body = request.getBody().readUtf8();
		var base64Content = body.replace("<QueueMessage><MessageText>", "").replace("</MessageText></QueueMessage>", "");
		var decoded = new String(Base64.getDecoder().decode(base64Content), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	@Test
	public void shouldSend_mTls() throws Exception {

		// arrange

		var tls = TlsMaterial.builder()
			.withEnabled(true).withWriteFiles(true)
			.withTrustStorePassword("changeit").withKeyStorePassword("changeit").withKeyPassword("changeit")
			.withServerHostNames(new String[] { "localhost", "127.0.0.1", "host.docker.internal", "kubernetes.docker.internal" })
			.build();

		startMockServerWithMtls(tls);
		mockServer.enqueue(new MockResponse().setResponseCode(201).setBody("<?xml version=\"1.0\"?><QueueMessagesList><QueueMessage><MessageId>id</MessageId></QueueMessage></QueueMessagesList>"));

		configureDestinationWithMtls("my-queue", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		var request = mockServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getPath()).isEqualTo("/devstoreaccount1/my-queue/messages");
		assertThat(request.getHeader("Authorization")).startsWith("SharedKey devstoreaccount1:");

		var body = request.getBody().readUtf8();
		var base64Content = body.replace("<QueueMessage><MessageText>", "").replace("</MessageText></QueueMessage>", "");
		var decoded = new String(Base64.getDecoder().decode(base64Content), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo("{\"type\":\"LOGIN\"}");
	}
}
