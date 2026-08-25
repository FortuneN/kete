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
class AzureWebPubSubDestinationE2ETests extends EndToEndTestBase {

	private static final String ACCESS_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
	private static final String HUB_NAME = "test_hub";

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
	void shouldForwardLoginEventToAzureWebPubSub() throws Exception {

		// arrange — enqueue enough 202 responses for the SDK requests

		for (var i = 0; i < 10; i++) {
			mockServer.enqueue(new MockResponse().setResponseCode(202));
		}

		var connectionString = "Endpoint=http://host.testcontainers.internal:" + mockServer.getPort()
			+ ";AccessKey=" + ACCESS_KEY + ";Version=1.0;";

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.wps-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.wps-test.destination.kind", "azure-webpubsub");
		envVars.put("kete.routes.wps-test.destination.connection-string", connectionString);
		envVars.put("kete.routes.wps-test.destination.hub", HUB_NAME);
		envVars.put("kete.routes.wps-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert

				okhttp3.mockwebserver.RecordedRequest request = null;
				for (var i = 0; i < 10; i++) {
					request = mockServer.takeRequest(30, TimeUnit.SECONDS);
					if (request != null && "POST".equals(request.getMethod())) {
						break;
					}
				}
				assertThat(request).isNotNull();
				assertThat(request.getMethod()).isEqualTo("POST");
				assertThat(request.getPath()).contains("/api/hubs/" + HUB_NAME);

				var body = request.getBody().readUtf8();
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("\"type\""),
					b -> assertThat(b).contains("\"operationType\"")
				);
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("\"realmName\""),
					b -> assertThat(b).contains("\"realmId\"")
				);
				assertThat(body).contains(TEST_REALM);

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}
}
