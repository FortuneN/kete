package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.testcontainers.Testcontainers;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SuppressWarnings("null")
class HttpDestinationOAuthE2ETests extends EndToEndTestBase {

	private static final String OAUTH_CLIENT_ID = "kete-oauth-client";
	private static final String OAUTH_CLIENT_SECRET = "kete-oauth-secret";

	private MockWebServer mockServer;

	@BeforeEach
	void setUp() throws Exception {
		mockServer = new MockWebServer();
		mockServer.start();
		Testcontainers.exposeHostPorts(mockServer.getPort());
	}

	@AfterEach
	void tearDown() throws Exception {
		if (mockServer != null) {
			mockServer.shutdown();
		}
		cleanupNetwork();
	}

	private void createOAuthClient(Keycloak adminClient) {
		var client = new ClientRepresentation();
		client.setClientId(OAUTH_CLIENT_ID);
		client.setSecret(OAUTH_CLIENT_SECRET);
		client.setEnabled(true);
		client.setServiceAccountsEnabled(true);
		client.setPublicClient(false);
		client.setDirectAccessGrantsEnabled(false);
		client.setStandardFlowEnabled(false);
		client.setClientAuthenticatorType("client-secret");
		client.setProtocol("openid-connect");

		var realm = adminClient.realm(TEST_REALM);
		realm.clients().create(client);
	}

	@Test
	void shouldForwardEventWithOAuthBearerToken() throws Exception {

		// arrange

		for (int i = 0; i < 5; i++) {
			mockServer.enqueue(new MockResponse().setResponseCode(200));
		}

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.oauth-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.oauth-test.destination.kind", "http");
		envVars.put("kete.routes.oauth-test.destination.host", "host.testcontainers.internal");
		envVars.put("kete.routes.oauth-test.destination.port", String.valueOf(mockServer.getPort()));
		envVars.put("kete.routes.oauth-test.destination.path-and-query", "/oauth-events");
		envVars.put("kete.routes.oauth-test.destination.method", "POST");
		envVars.put("kete.routes.oauth-test.serializer.kind", "json");

		envVars.put("kete.routes.oauth-test.destination.authentication-type", "oauth");
		envVars.put("kete.routes.oauth-test.destination.oauth.enabled", "true");
		envVars.put("kete.routes.oauth-test.destination.oauth.client-id", OAUTH_CLIENT_ID);
		envVars.put("kete.routes.oauth-test.destination.oauth.client-secret", OAUTH_CLIENT_SECRET);
		envVars.put("kete.routes.oauth-test.destination.oauth.token-url", "http://localhost:8080/realms/" + TEST_REALM + "/protocol/openid-connect/token");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(
				keycloak.getAuthServerUrl(),
				"master",
				keycloak.getAdminUsername(),
				keycloak.getAdminPassword(),
				"admin-cli"
			)) {
				createTestRealm(adminClient);

				createOAuthClient(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert

				RecordedRequest request = null;
				for (int i = 0; i < 10; i++) {
					request = mockServer.takeRequest(30, TimeUnit.SECONDS);
					if (request != null && "POST".equals(request.getMethod()) && "/oauth-events".equals(request.getPath())) {
						break;
					}
				}

				assertThat(request)
					.as("Should receive a POST request to /oauth-events")
					.isNotNull();

				assertThat(request.getMethod())
					.as("Request method should be POST")
					.isEqualTo("POST");

				assertThat(request.getPath())
					.as("Request path should be /oauth-events")
					.isEqualTo("/oauth-events");

				var authHeader = request.getHeader("Authorization");
				assertThat(authHeader)
					.as("Authorization header should be present")
					.isNotNull()
					.isNotBlank();

				assertThat(authHeader)
					.as("Authorization header should contain Bearer token")
					.startsWith("Bearer ");

				var token = authHeader.substring("Bearer ".length());
				assertThat(token)
					.as("Bearer token should not be empty")
					.isNotBlank();

				assertThat(token.split("\\."))
					.as("Token should be a valid JWT with 3 parts (header.payload.signature)")
					.hasSize(3);

				var body = request.getBody().readUtf8();
				assertThat(body)
					.as("Event body should contain event type or operation type")
					.satisfiesAnyOf(b -> assertThat(b).contains("\"type\""), b -> assertThat(b).contains("\"operationType\""));

				assertThat(body)
					.as("Event body should contain realm information")
					.contains(TEST_REALM);

				var contentType = request.getHeader("Content-Type");
				assertThat(contentType)
					.as("Content-Type header should be present")
					.isNotNull()
					.contains("json");

				var eventTypeHeader = request.getHeader("x-eventtype");
				assertThat(eventTypeHeader)
					.as("x-eventtype header should be present")
					.isNotNull()
					.isNotBlank();

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}

	@Test
	void shouldForwardAdminEventWithOAuthBearerToken() throws Exception {

		// arrange

		for (int i = 0; i < 10; i++) {
			mockServer.enqueue(new MockResponse().setResponseCode(200));
		}

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.oauth-admin-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.oauth-admin-test.destination.kind", "http");
		envVars.put("kete.routes.oauth-admin-test.destination.host", "host.testcontainers.internal");
		envVars.put("kete.routes.oauth-admin-test.destination.port", String.valueOf(mockServer.getPort()));
		envVars.put("kete.routes.oauth-admin-test.destination.path-and-query", "/admin-events");
		envVars.put("kete.routes.oauth-admin-test.destination.method", "POST");
		envVars.put("kete.routes.oauth-admin-test.serializer.kind", "json");

		envVars.put("kete.routes.oauth-admin-test.destination.authentication-type", "oauth");
		envVars.put("kete.routes.oauth-admin-test.destination.oauth.enabled", "true");
		envVars.put("kete.routes.oauth-admin-test.destination.oauth.client-id", OAUTH_CLIENT_ID);
		envVars.put("kete.routes.oauth-admin-test.destination.oauth.client-secret", OAUTH_CLIENT_SECRET);
		envVars.put("kete.routes.oauth-admin-test.destination.oauth.token-url", "http://localhost:8080/realms/" + TEST_REALM + "/protocol/openid-connect/token");

		envVars.put("kete.routes.oauth-admin-test.matchers.admin.kind", "glob");
		envVars.put("kete.routes.oauth-admin-test.matchers.admin.pattern", "*_USER");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(
				keycloak.getAuthServerUrl(),
				"master",
				keycloak.getAdminUsername(),
				keycloak.getAdminPassword(),
				"admin-cli"
			)) {
				createTestRealm(adminClient);

				createOAuthClient(adminClient);

				// act

				triggerAdminEvent(adminClient);

				// assert

				RecordedRequest request = null;
				String body = null;
				for (int i = 0; i < 15; i++) {
					request = mockServer.takeRequest(30, TimeUnit.SECONDS);
					if (request != null && "POST".equals(request.getMethod()) && "/admin-events".equals(request.getPath())) {
						body = request.getBody().readUtf8();
						if (body.contains("\"operationType\"") && body.contains("\"resourceType\":\"USER\"")) {
							break;
						}
						request = null;
					}
				}

				assertThat(request)
					.as("Should receive a POST request to /admin-events with a USER admin event")
					.isNotNull();

				String authHeader = request.getHeader("Authorization");
				assertThat(authHeader)
					.as("Authorization header should be present with Bearer token")
					.isNotNull()
					.startsWith("Bearer ");

				String token = authHeader.substring("Bearer ".length());
				assertThat(token.split("\\."))
					.as("Token should be a valid JWT")
					.hasSize(3);

				assertThat(body)
					.as("Admin event should contain operationType")
					.contains("\"operationType\"");

				assertThat(body)
					.as("Admin event should be about USER resource")
					.contains("USER");

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}

	@Test
	void shouldRefreshTokenWhenExpired() throws Exception {

		// arrange

		for (int i = 0; i < 20; i++) {
			mockServer.enqueue(new MockResponse().setResponseCode(200));
		}

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.token-refresh-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.token-refresh-test.destination.kind", "http");
		envVars.put("kete.routes.token-refresh-test.destination.host", "host.testcontainers.internal");
		envVars.put("kete.routes.token-refresh-test.destination.port", String.valueOf(mockServer.getPort()));
		envVars.put("kete.routes.token-refresh-test.destination.path-and-query", "/events");
		envVars.put("kete.routes.token-refresh-test.destination.method", "POST");
		envVars.put("kete.routes.token-refresh-test.serializer.kind", "json");

		envVars.put("kete.routes.token-refresh-test.destination.authentication-type", "oauth");
		envVars.put("kete.routes.token-refresh-test.destination.oauth.enabled", "true");
		envVars.put("kete.routes.token-refresh-test.destination.oauth.client-id", OAUTH_CLIENT_ID);
		envVars.put("kete.routes.token-refresh-test.destination.oauth.client-secret", OAUTH_CLIENT_SECRET);
		envVars.put("kete.routes.token-refresh-test.destination.oauth.token-url", "http://localhost:8080/realms/" + TEST_REALM + "/protocol/openid-connect/token");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);
				createOAuthClient(adminClient);

				// act

				triggerLoginEvent(keycloak);
				triggerLoginEvent(keycloak);
				triggerLoginEvent(keycloak);

				// assert

				int eventsWithValidTokens = 0;
				String previousToken = null;

				for (int i = 0; i < 15; i++) {
					RecordedRequest request = mockServer.takeRequest(10, TimeUnit.SECONDS);
					if (request == null) {
						break;
					}

					if ("POST".equals(request.getMethod()) && "/events".equals(request.getPath())) {
						String authHeader = request.getHeader("Authorization");
						if (authHeader != null && authHeader.startsWith("Bearer ")) {
							eventsWithValidTokens++;

							String token = authHeader.substring("Bearer ".length());
							assertThat(token.split("\\."))
							.as("Each token should be a valid JWT")
							.hasSize(3);

							if (previousToken != null) {
								assertThat(token).isNotBlank();
							}
							previousToken = token;
						}
					}
				}

				assertThat(eventsWithValidTokens)
					.as("Should receive at least one event with valid OAuth token")
					.isGreaterThanOrEqualTo(1);

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}
}
