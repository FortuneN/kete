package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.utils.AzureStorageQueueUtils;

class AzureStorageQueueDestinationE2ETests extends EndToEndTestBase {

	private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";
	private static final String ACCOUNT_NAME = "devstoreaccount1";
	private static final String ACCOUNT_KEY = AzureStorageQueueUtils.WELL_KNOWN_ACCOUNT_KEY;
	private static final int QUEUE_PORT = 10001;
	private static final String API_VERSION = "2024-08-04";
	private static final DateTimeFormatter RFC_1123_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

	private GenericContainer<?> azurite;
	private SecretKeySpec testKeySpec;

	@AfterEach
	void tearDown() {
		if (azurite != null) {
			azurite.stop();
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToAzureStorageQueue() throws Exception {

		// arrange

		testKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(ACCOUNT_KEY);

		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("azurite")
			.withExposedPorts(QUEUE_PORT)
			.withCommand("azurite-queue", "--queueHost", "0.0.0.0", "--queuePort", String.valueOf(QUEUE_PORT))
			.waitingFor(Wait.forListeningPort())
			.withStartupTimeout(CONTAINER_STARTUP_TIMEOUT);
		azurite.start();

		waitForAzuriteReady();
		createQueue("test-queue");

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.asq-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.asq-test.destination.kind", "azure-storage-queue");
		envVars.put("kete.routes.asq-test.destination.account-name", ACCOUNT_NAME);
		envVars.put("kete.routes.asq-test.destination.account-key", ACCOUNT_KEY);
		envVars.put("kete.routes.asq-test.destination.url", "http://azurite:" + QUEUE_PORT + "/" + ACCOUNT_NAME);
		envVars.put("kete.routes.asq-test.destination.queue", "test-queue");
		envVars.put("kete.routes.asq-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — poll Azurite for the message

				await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
					var response = peekMessage("test-queue");
					assertThat(response).contains("<QueueMessage>");

					var messageText = extractMessageText(response);
					var decoded = new String(Base64.getDecoder().decode(messageText), StandardCharsets.UTF_8);
					assertThat(decoded).satisfiesAnyOf(
						b -> assertThat(b).contains("\"type\""),
						b -> assertThat(b).contains("\"operationType\"")
					);
					assertThat(decoded).satisfiesAnyOf(
						b -> assertThat(b).contains("\"realmName\""),
						b -> assertThat(b).contains("\"realmId\"")
					);
					assertThat(decoded).contains(TEST_REALM);
				});

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}

	private void waitForAzuriteReady() {
		var baseUrl = getAzuriteBaseUrl();
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
				HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void createQueue(String queue) throws Exception {
		var url = getAzuriteBaseUrl() + "/" + queue;
		var request = authenticatedRequest("PUT", url, queue)
			.PUT(HttpRequest.BodyPublishers.noBody())
			.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 201 && response.statusCode() != 204) {
			throw new RuntimeException("Failed to create queue '" + queue + "': HTTP " + response.statusCode() + " " + response.body());
		}
	}

	private String peekMessage(String queue) throws Exception {
		var url = getAzuriteBaseUrl() + "/" + queue + "/messages?peekonly=true";
		var request = authenticatedRequest("GET", url, queue + "/messages\npeekonly:true")
			.GET()
			.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	private String getAzuriteBaseUrl() {
		return "http://" + azurite.getHost() + ":" + azurite.getMappedPort(QUEUE_PORT) + "/" + ACCOUNT_NAME;
	}

	private HttpRequest.Builder authenticatedRequest(String verb, String url, String canonicalResource) {
		var date = ZonedDateTime.now(ZoneOffset.UTC).format(RFC_1123_FORMATTER);
		var stringToSign = verb + "\n\n\n\n\n\n\n\n\n\n\n\n"
			+ "x-ms-date:" + date + "\n"
			+ "x-ms-version:" + API_VERSION + "\n"
			+ "/" + ACCOUNT_NAME + "/" + ACCOUNT_NAME + "/" + canonicalResource;
		var signature = AzureStorageQueueUtils.computeSignature(testKeySpec, stringToSign);
		return HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("x-ms-date", date)
			.header("x-ms-version", API_VERSION)
			.header("Authorization", "SharedKey " + ACCOUNT_NAME + ":" + signature);
	}

	private String extractMessageText(String xml) {
		var start = xml.indexOf("<MessageText>") + "<MessageText>".length();
		var end = xml.indexOf("</MessageText>");
		return xml.substring(start, end);
	}
}
