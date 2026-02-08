package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;

import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

@SuppressWarnings("null")
class AzureStorageQueueDestinationE2ETests extends EndToEndTestBase {

	private static final String VALID_ACCOUNT_KEY = AzureStorageQueueUtils.WELL_KNOWN_ACCOUNT_KEY;

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
	void shouldForwardLoginEventToAzureStorageQueue() throws Exception {

		// arrange — enqueue enough responses: 1 GET (init verify) + several POSTs (events)

		for (var i = 0; i < 10; i++) {
			mockServer.enqueue(new MockResponse().setResponseCode(201));
		}

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.asq-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.asq-test.destination.kind", "azure-storage-queue");
		envVars.put("kete.routes.asq-test.destination.account-name", "testaccount");
		envVars.put("kete.routes.asq-test.destination.account-key", VALID_ACCOUNT_KEY);
		envVars.put("kete.routes.asq-test.destination.url", "http://host.testcontainers.internal:" + mockServer.getPort());
		envVars.put("kete.routes.asq-test.destination.queue", "test-queue");
		envVars.put("kete.routes.asq-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — find the POST to /test-queue/messages

				RecordedRequest request = null;
				for (var i = 0; i < 10; i++) {
					request = mockServer.takeRequest(30, TimeUnit.SECONDS);
					if (request != null && "POST".equals(request.getMethod())) {
						break;
					}
				}
				assertThat(request).isNotNull();
				assertThat(request.getMethod()).isEqualTo("POST");
				assertThat(request.getPath()).startsWith("/test-queue/messages");
				assertThat(request.getHeader("Authorization")).startsWith("SharedKey testaccount:");
				assertThat(request.getHeader("x-ms-version")).isEqualTo("2024-08-04");
				assertThat(request.getHeader("x-ms-date")).isNotBlank();
				assertThat(request.getHeader("Content-Type")).isEqualTo("application/xml");

				var body = request.getBody().readUtf8();
				assertThat(body).startsWith("<QueueMessage><MessageText>");
				assertThat(body).endsWith("</MessageText></QueueMessage>");

				var base64Content = body.replace("<QueueMessage><MessageText>", "").replace("</MessageText></QueueMessage>", "");
				var decodedEvent = new String(Base64.getDecoder().decode(base64Content));
				assertThat(decodedEvent).satisfiesAnyOf(
					b -> assertThat(b).contains("\"type\""),
					b -> assertThat(b).contains("\"operationType\"")
				);
				assertThat(decodedEvent).satisfiesAnyOf(
					b -> assertThat(b).contains("\"realmName\""),
					b -> assertThat(b).contains("\"realmId\"")
				);
				assertThat(decodedEvent).contains(TEST_REALM);

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}
}
