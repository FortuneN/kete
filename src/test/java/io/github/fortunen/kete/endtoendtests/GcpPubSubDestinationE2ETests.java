package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.awaitility.Awaitility.await;

@SuppressWarnings("resource")
class GcpPubSubDestinationE2ETests extends EndToEndTestBase {

	private static final String PROJECT_ID = "test-project";
	private static final String TOPIC_ID = "keycloak-events";
	private static final String SUBSCRIPTION_ID = "keycloak-events-sub";
	private static final int EMULATOR_PORT = 8085;
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private GenericContainer<?> pubsubEmulator;

	@AfterEach
	void tearDown() {
		if (pubsubEmulator != null) {
			pubsubEmulator.stop();
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToGcpPubSub() throws Exception {

		// arrange

		pubsubEmulator = new GenericContainer<>(DockerImageName.parse("google/cloud-sdk:emulators"))
			.withNetwork(createNetwork())
			.withNetworkAliases("pubsub-emulator")
			.withExposedPorts(EMULATOR_PORT)
			.withCommand("gcloud", "beta", "emulators", "pubsub", "start",
				"--project=" + PROJECT_ID,
				"--host-port=0.0.0.0:" + EMULATOR_PORT)
			.waitingFor(Wait.forLogMessage(".*Server started.*", 1))
			.withStartupTimeout(CONTAINER_STARTUP_TIMEOUT);

		pubsubEmulator.start();

		var emulatorHost = pubsubEmulator.getHost();
		var emulatorPort = pubsubEmulator.getMappedPort(EMULATOR_PORT);
		var emulatorUrl = "http://" + emulatorHost + ":" + emulatorPort;

		waitForEmulatorReady(emulatorUrl);

		// create topic and subscription via emulator REST API

		createTopic(emulatorUrl, PROJECT_ID, TOPIC_ID);
		createSubscription(emulatorUrl, PROJECT_ID, TOPIC_ID, SUBSCRIPTION_ID);

		// configure keycloak

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.pubsub-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.pubsub-test.destination.kind", "gcp-pubsub");
		envVars.put("kete.routes.pubsub-test.destination.project", PROJECT_ID);
		envVars.put("kete.routes.pubsub-test.destination.topic", TOPIC_ID);
		envVars.put("kete.routes.pubsub-test.destination.url", "http://pubsub-emulator:" + EMULATOR_PORT);
		envVars.put("kete.routes.pubsub-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert

				await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
					var messages = pullMessages(emulatorUrl, PROJECT_ID, SUBSCRIPTION_ID);
					assertThat(messages).isNotEmpty();
					assertThat(messages).anyMatch(msg -> msg.contains(TEST_REALM));
				});

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}

	private void waitForEmulatorReady(String emulatorUrl) {

		var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1)).until(() -> {

			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(emulatorUrl))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();

				client.send(request, HttpResponse.BodyHandlers.discarding());
				return true;

			} catch (Exception e) {
				return false;
			}
		});
	}

	private void createTopic(String emulatorUrl, String projectId, String topicId) throws Exception {

		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder()
			.uri(URI.create(emulatorUrl + "/v1/projects/" + projectId + "/topics/" + topicId))
			.PUT(HttpRequest.BodyPublishers.ofString("{}"))
			.header("Content-Type", "application/json")
			.build();

		var response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode())
			.as("Create topic response: " + response.body())
			.isIn(200, 409); // 409 = already exists
	}

	private void createSubscription(String emulatorUrl, String projectId, String topicId, String subscriptionId) throws Exception {

		var client = HttpClient.newHttpClient();
		var body = "{\"topic\":\"projects/" + projectId + "/topics/" + topicId + "\"}";
		var request = HttpRequest.newBuilder()
			.uri(URI.create(emulatorUrl + "/v1/projects/" + projectId + "/subscriptions/" + subscriptionId))
			.PUT(HttpRequest.BodyPublishers.ofString(body))
			.header("Content-Type", "application/json")
			.build();

		var response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode())
			.as("Create subscription response: " + response.body())
			.isIn(200, 409); // 409 = already exists
	}

	private java.util.List<String> pullMessages(String emulatorUrl, String projectId, String subscriptionId) throws Exception {

		var client = HttpClient.newHttpClient();
		var body = "{\"maxMessages\":10}";
		var request = HttpRequest.newBuilder()
			.uri(URI.create(emulatorUrl + "/v1/projects/" + projectId + "/subscriptions/" + subscriptionId + ":pull"))
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.header("Content-Type", "application/json")
			.build();

		var response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			return java.util.List.of();
		}

		var root = MAPPER.readTree(response.body());
		var receivedMessages = root.get("receivedMessages");

		if (receivedMessages == null || receivedMessages.isEmpty()) {
			return java.util.List.of();
		}

		var messages = new java.util.ArrayList<String>();

		for (var receivedMessage : receivedMessages) {
			var data = receivedMessage.get("message").get("data").asText();
			var decoded = new String(java.util.Base64.getDecoder().decode(data), java.nio.charset.StandardCharsets.UTF_8);
			messages.add(decoded);
		}

		return messages;
	}
}
