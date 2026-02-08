package io.github.fortunen.kete.integrationtests.azurestoragequeuedestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startAzurite();
		createQueue("my-queue");
		configureDestination("my-queue");
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		var response = peekMessage("my-queue");
		assertThat(response).contains("<QueueMessage>");
		var messageText = extractMessageText(response);
		var decoded = new String(Base64.getDecoder().decode(messageText), StandardCharsets.UTF_8);
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

		startAzuriteOnNetwork();
		startNginxTlsProxy(tls, false);
		createQueue("my-queue");
		configureDestinationWithTls("my-queue", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read directly from Azurite (plain HTTP) to verify

		var response = peekMessage("my-queue");
		assertThat(response).contains("<QueueMessage>");
		var messageText = extractMessageText(response);
		var decoded = new String(Base64.getDecoder().decode(messageText), StandardCharsets.UTF_8);
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

		startAzuriteOnNetwork();
		startNginxTlsProxy(tls, true);
		createQueue("my-queue");
		configureDestinationWithMtls("my-queue", tls);
		destination.initialize();

		var message = createMessage(
			"test-event-id", "LOGIN", "application/json",
			"{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert — read directly from Azurite (plain HTTP) to verify

		var response = peekMessage("my-queue");
		assertThat(response).contains("<QueueMessage>");
		var messageText = extractMessageText(response);
		var decoded = new String(Base64.getDecoder().decode(messageText), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo("{\"type\":\"LOGIN\"}");
	}

	private String extractMessageText(String xml) {
		var start = xml.indexOf("<MessageText>") + "<MessageText>".length();
		var end = xml.indexOf("</MessageText>");
		return xml.substring(start, end);
	}
}
