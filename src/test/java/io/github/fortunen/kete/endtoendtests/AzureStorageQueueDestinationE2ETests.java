package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.QueueMessageEncoding;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

class AzureStorageQueueDestinationE2ETests extends EndToEndTestBase {

	private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";
	private static final String ACCOUNT_NAME = "devstoreaccount1";
	private static final String ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
	private static final int QUEUE_PORT = 10001;

	private GenericContainer<?> azurite;

	@AfterEach
	void tearDown() {
		if (azurite != null) {
			azurite.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToAzureStorageQueue() throws Exception {

		// arrange

		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("azurite")
			.withExposedPorts(QUEUE_PORT)
			.withCommand("azurite-queue", "--queueHost", "0.0.0.0", "--queuePort", String.valueOf(QUEUE_PORT));
		azurite.start();

		waitForAzuriteReady();
		createQueue("test-queue");

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.asq-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.asq-test.destination.kind", "azure-storage-queue");
		envVars.put("kete.routes.asq-test.destination.connection-string", "DefaultEndpointsProtocol=http;AccountName=" + ACCOUNT_NAME + ";AccountKey=" + ACCOUNT_KEY + ";QueueEndpoint=http://azurite:" + QUEUE_PORT + "/" + ACCOUNT_NAME);
		envVars.put("kete.routes.asq-test.destination.queue", "test-queue");
		envVars.put("kete.routes.asq-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — poll Azurite for the message

				await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
					var body = peekMessage("test-queue");
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("\"type\""),
						b -> assertThat(b).contains("\"operationType\"")
					);
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("\"realmName\""),
						b -> assertThat(b).contains("\"realmId\"")
					);
					assertThat(body).contains(TEST_REALM);
				});

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}

	private void waitForAzuriteReady() {
		var baseUrl = getAzuriteBaseUrl();
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
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

	private void createQueue(String queue) {
		new QueueClientBuilder()
			.connectionString(buildVerificationConnectionString())
			.queueName(queue)
			.buildClient()
			.create();
	}

	private String peekMessage(String queue) {
		return new QueueClientBuilder()
			.connectionString(buildVerificationConnectionString())
			.queueName(queue)
			.messageEncoding(QueueMessageEncoding.BASE64)
			.buildClient()
			.peekMessage()
			.getBody()
			.toString();
	}

	private String getAzuriteBaseUrl() {
		return "http://" + "127.0.0.1" + ":" + azurite.getMappedPort(QUEUE_PORT) + "/" + ACCOUNT_NAME;
	}

	private String buildVerificationConnectionString() {
		return "DefaultEndpointsProtocol=http;AccountName=" + ACCOUNT_NAME + ";AccountKey=" + ACCOUNT_KEY + ";QueueEndpoint=" + getAzuriteBaseUrl();
	}
}
