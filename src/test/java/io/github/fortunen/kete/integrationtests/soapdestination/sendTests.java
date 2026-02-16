package io.github.fortunen.kete.integrationtests.soapdestination;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import okhttp3.mockwebserver.MockResponse;

public class sendTests extends TestBase {

	@Test
	public void shouldSend_NonTls() throws Exception {

		// arrange

		startMockServer();
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for initialize() test request
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for send() request
		configureDestinationWithMockServer();
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/xml",
			"<event><type>LOGIN</type></event>".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // discard initialize() test request
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		var body = request.getBody().readUtf8();
		assertThat(body).contains("<soap:Envelope");
		assertThat(body).contains("xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"");
		assertThat(body).contains("<soap:Body>");
		assertThat(body).contains("<event><type>LOGIN</type></event>");
		assertThat(body).contains("</soap:Body>");
		assertThat(body).contains("</soap:Envelope>");
		assertThat(request.getHeader("Content-Type")).isEqualTo("text/xml");
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
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for initialize() test request
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for send() request

		var map = new HashMap<String, Object>();
		map.put("kind", "soap");
		map.put("host", mockServer.getHostName());
		map.put("port", mockServer.getPort());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/xml",
			"<event><type>LOGIN</type></event>".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // discard initialize() test request
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		var body = request.getBody().readUtf8();
		assertThat(body).contains("<soap:Envelope");
		assertThat(body).contains("<soap:Body>");
		assertThat(body).contains("<event><type>LOGIN</type></event>");
		assertThat(request.getHeader("Content-Type")).isEqualTo("text/xml");
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
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for initialize() test request
		mockServer.enqueue(new MockResponse().setResponseCode(200)); // for send() request

		var map = new HashMap<String, Object>();
		map.put("kind", "soap");
		map.put("host", mockServer.getHostName());
		map.put("port", mockServer.getPort());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-base64");
		map.put("tls.trust-store.loader.base64", tls.getTrustStoreBase64());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-base64");
		map.put("tls.key-store.loader.base64", tls.getKeyStoreBase64());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		var mapConfig = new MapConfiguration(map);
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
		destination.initialize();

		var message = createMessage(
			"test-event-id",
			"LOGIN",
			"application/xml",
			"<event><type>LOGIN</type></event>".getBytes(StandardCharsets.UTF_8)
		);

		// act

		destination.send(message);

		// assert

		mockServer.takeRequest(1, TimeUnit.MINUTES); // discard initialize() test request
		var request = mockServer.takeRequest(1, TimeUnit.MINUTES);
		assertThat(request.getMethod()).isEqualTo("POST");
		var body = request.getBody().readUtf8();
		assertThat(body).contains("<soap:Envelope");
		assertThat(body).contains("<soap:Body>");
		assertThat(body).contains("<event><type>LOGIN</type></event>");
		assertThat(request.getHeader("Content-Type")).isEqualTo("text/xml");
	}
}
