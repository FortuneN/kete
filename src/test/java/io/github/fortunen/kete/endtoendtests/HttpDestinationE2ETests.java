package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;

@SuppressWarnings("null")
class HttpDestinationE2ETests extends EndToEndTestBase {

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

	@Test
	void shouldForwardLoginEventToHttpEndpoint() throws Exception {

		// arrange

		mockServer.enqueue(new MockResponse().setResponseCode(200));
		mockServer.enqueue(new MockResponse().setResponseCode(200));

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.http-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.http-test.destination.kind", "http");
		envVars.put("kete.routes.http-test.destination.host", "host.testcontainers.internal");
		envVars.put("kete.routes.http-test.destination.port", String.valueOf(mockServer.getPort()));
		envVars.put("kete.routes.http-test.destination.path-and-query", "/events");
		envVars.put("kete.routes.http-test.destination.method", "POST");
		envVars.put("kete.routes.http-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert

				okhttp3.mockwebserver.RecordedRequest request = null;
				for (int i = 0; i < 10; i++) {
					request = mockServer.takeRequest(30, TimeUnit.SECONDS);
					if (request != null && "POST".equals(request.getMethod())) {
						break;
					}
				}
				assertThat(request).isNotNull();
				assertThat(request.getMethod()).isEqualTo("POST");
				assertThat(request.getPath()).isEqualTo("/events");

				var body = request.getBody().readUtf8();
				assertThat(body).satisfiesAnyOf(b -> assertThat(b).contains("\"type\""), b -> assertThat(b).contains("\"operationType\""));
				assertThat(body).satisfiesAnyOf(b -> assertThat(b).contains("\"realmName\""), b -> assertThat(b).contains("\"realmId\""));
				assertThat(body).contains(TEST_REALM);

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}
}
