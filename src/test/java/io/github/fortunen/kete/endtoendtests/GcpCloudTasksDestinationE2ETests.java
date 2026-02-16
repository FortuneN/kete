package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.awaitility.Awaitility.await;

class GcpCloudTasksDestinationE2ETests extends EndToEndTestBase {

	private static final String EMULATOR_IMAGE = "ghcr.io/aertje/cloud-tasks-emulator:latest";
	private static final int EMULATOR_PORT = 8123;
	private static final String PROJECT = "test-project";
	private static final String LOCATION = "us-central1";
	private static final String QUEUE = "test-queue";
	private static final String QUEUE_PATH = "projects/" + PROJECT + "/locations/" + LOCATION + "/queues/" + QUEUE;

	private GenericContainer<?> emulator;
	private MockWebServer taskReceiver;

	@AfterEach
	void tearDown() throws Exception {
		if (taskReceiver != null) {
			taskReceiver.shutdown();
		}
		if (emulator != null) {
			emulator.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToGcpCloudTasks() throws Exception {

		// arrange — start HTTP receiver for dispatched tasks

		taskReceiver = new MockWebServer();
		taskReceiver.start();
		for (var i = 0; i < 10; i++) {
			taskReceiver.enqueue(new MockResponse().setResponseCode(200));
		}
		Testcontainers.exposeHostPorts(taskReceiver.getPort());

		// start emulator on the Docker network

		emulator = new GenericContainer<>(DockerImageName.parse(EMULATOR_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("cloud-tasks-emulator")
			.withExposedPorts(EMULATOR_PORT)
			.withCommand(
				"-host", "0.0.0.0",
				"-port", String.valueOf(EMULATOR_PORT),
				"-queue", QUEUE_PATH);
		emulator.start();

		waitForPortReady("127.0.0.1", emulator.getMappedPort(EMULATOR_PORT));

		// configure keycloak — destination uses emulator via Docker network alias

		var targetUrl = "http://host.testcontainers.internal:" + taskReceiver.getPort() + "/handler";

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.tasks-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.tasks-test.destination.kind", "gcp-cloud-tasks");
		envVars.put("kete.routes.tasks-test.destination.project", PROJECT);
		envVars.put("kete.routes.tasks-test.destination.location", LOCATION);
		envVars.put("kete.routes.tasks-test.destination.queue", QUEUE);
		envVars.put("kete.routes.tasks-test.destination.target-url", targetUrl);
		envVars.put("kete.routes.tasks-test.destination.endpoint", "cloud-tasks-emulator:" + EMULATOR_PORT);
		envVars.put("kete.routes.tasks-test.destination.use-plaintext", "true");
		envVars.put("kete.routes.tasks-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — emulator dispatches the task to our HTTP receiver

				var dispatchedRequest = await()
					.atMost(Duration.ofMinutes(2))
					.pollInterval(Duration.ofSeconds(2))
					.until(() -> {
						try {
							return taskReceiver.takeRequest(1, TimeUnit.SECONDS);
						} catch (Exception e) {
							return null;
						}
					}, req -> req != null);

				assertThat(dispatchedRequest).isNotNull();
				assertThat(dispatchedRequest.getMethod()).isEqualTo("POST");

				var body = dispatchedRequest.getBody().readUtf8();
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
