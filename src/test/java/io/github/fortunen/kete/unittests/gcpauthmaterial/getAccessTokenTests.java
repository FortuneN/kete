package io.github.fortunen.kete.unittests.gcpauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.GcpAuthMaterial;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class getAccessTokenTests {

	private MockWebServer mockTokenServer;

	@AfterEach
	void tearDown() throws Exception {
		if (mockTokenServer != null) {
			mockTokenServer.shutdown();
		}
	}

	private GcpAuthMaterial createAuthMaterial() throws Exception {

		mockTokenServer = new MockWebServer();
		mockTokenServer.start();

		var heldCert = new HeldCertificate.Builder().commonName("test").rsa2048().build();
		var privateKeyPem = heldCert.privateKeyPkcs8Pem().replace("\n", "\\n");
		var tokenUrl = "http://" + mockTokenServer.getHostName() + ":" + mockTokenServer.getPort() + "/token";

		var json = """
			{
				"type": "service_account",
				"client_email": "test@test-project.iam.gserviceaccount.com",
				"token_uri": "%s",
				"private_key": "%s"
			}
			""".formatted(tokenUrl, privateKeyPem);

		return GcpAuthMaterial.fromCredentialsText(json);
	}

	@Test
	public void shouldReturnAccessToken() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"access_token\":\"mock-token-123\",\"token_type\":\"Bearer\",\"expires_in\":3600}"));

		// act

		var token = material.getAccessToken();

		// assert

		assertThat(token).isNotNull();
		assertThat(token.getValue()).isEqualTo("mock-token-123");
	}

	@Test
	public void shouldReturnBearerAuthorizationHeader() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"access_token\":\"mock-token-123\",\"token_type\":\"Bearer\",\"expires_in\":3600}"));

		// act

		var token = material.getAccessToken();

		// assert

		assertThat(token.toAuthorizationHeader()).isEqualTo("Bearer mock-token-123");
	}

	@Test
	public void shouldReturnTokenLifetime() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"access_token\":\"mock-token-123\",\"token_type\":\"Bearer\",\"expires_in\":7200}"));

		// act

		var token = material.getAccessToken();

		// assert

		assertThat(token.getLifetime()).isEqualTo(7200L);
	}

	@Test
	public void shouldSendJwtBearerGrant() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"access_token\":\"mock-token-123\",\"token_type\":\"Bearer\",\"expires_in\":3600}"));

		// act

		material.getAccessToken();

		// assert

		var request = mockTokenServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");

		var body = request.getBody().readUtf8();
		assertThat(body).contains("grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer");
		assertThat(body).contains("assertion=");
	}

	@Test
	public void shouldSendFormUrlEncodedContentType() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"access_token\":\"mock-token-123\",\"token_type\":\"Bearer\",\"expires_in\":3600}"));

		// act

		material.getAccessToken();

		// assert

		var request = mockTokenServer.takeRequest();
		assertThat(request.getHeader("Content-Type")).contains("application/x-www-form-urlencoded");
	}

	@Test
	public void shouldCacheTokenOnSubsequentCalls() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"access_token\":\"cached-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}"));

		// act

		var firstToken = material.getAccessToken();
		var secondToken = material.getAccessToken();

		// assert

		assertThat(firstToken.getValue()).isEqualTo("cached-token");
		assertThat(secondToken.getValue()).isEqualTo("cached-token");
		assertThat(mockTokenServer.getRequestCount()).isEqualTo(1);
	}

	@Test
	public void shouldThrowWhenTokenEndpointReturnsError() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(400)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"error\":\"invalid_grant\",\"error_description\":\"JWT expired\"}"));

		// act

		var thrown = catchThrowable(() -> material.getAccessToken());

		// assert

		assertThat(thrown).isNotNull();
	}

	@Test
	public void shouldPostToConfiguredTokenUri() throws Exception {

		// arrange

		var material = createAuthMaterial();

		mockTokenServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"access_token\":\"mock-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}"));

		// act

		material.getAccessToken();

		// assert

		var request = mockTokenServer.takeRequest();
		assertThat(request.getPath()).isEqualTo("/token");
	}
}
