package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.RuleState;
import software.amazon.awssdk.services.eventbridge.model.Target;
import software.amazon.awssdk.services.sqs.SqsClient;

class AwsEventBridgeDestinationE2ETests extends EndToEndTestBase {

	private static final String LOCALSTACK_IMAGE = "localstack/localstack";
	private static final int LOCALSTACK_PORT = 4566;
	private static final String REGION = "us-east-1";
	private static final String ACCOUNT_ID = "000000000000";
	private static final String EVENT_BUS_NAME = "test-bus";
	private static final String VERIFY_QUEUE = "verify-queue";
	private static final String RULE_NAME = "catch-all";

	private GenericContainer<?> localstack;
	private SqsClient sqsClient;
	private EventBridgeClient eventBridgeClient;

	@AfterEach
	void tearDown() {
		if (eventBridgeClient != null) {
			try { eventBridgeClient.close(); } catch (Exception e) { /* ignore */ }
			eventBridgeClient = null;
		}
		if (sqsClient != null) {
			try { sqsClient.close(); } catch (Exception e) { /* ignore */ }
			sqsClient = null;
		}
		if (localstack != null) {
			localstack.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToAwsEventBridge() throws Exception {

		// arrange

		localstack = new GenericContainer<>(DockerImageName.parse(LOCALSTACK_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("localstack")
			.withExposedPorts(LOCALSTACK_PORT)
			.withEnv("SERVICES", "events,sqs");
		localstack.start();

		waitForLocalStackReady();
		sqsClient = buildVerificationSqsClient();
		eventBridgeClient = buildVerificationEventBridgeClient();
		createEventBusAndVerificationQueue();

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.eb-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.eb-test.destination.kind", "aws-eventbridge");
		envVars.put("kete.routes.eb-test.destination.event-bus", EVENT_BUS_NAME);
		envVars.put("kete.routes.eb-test.destination.source", "kete.keycloak");
		envVars.put("kete.routes.eb-test.destination.detail-type", "KeycloakEvent");
		envVars.put("kete.routes.eb-test.destination.region", REGION);
		envVars.put("kete.routes.eb-test.destination.endpoint-url", "http://localstack:" + LOCALSTACK_PORT);
		envVars.put("kete.routes.eb-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — poll SQS verification queue for the EventBridge-routed message

				await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
					var body = receiveVerificationMessage();
					assertThat(body).isNotEmpty();
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

	private void waitForLocalStackReady() {
		var baseUrl = getLocalStackBaseUrl();
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "/_localstack/health"))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
				var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
				return response.statusCode() == 200;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void createEventBusAndVerificationQueue() {
		sqsClient.createQueue(r -> r.queueName(VERIFY_QUEUE));

		eventBridgeClient.createEventBus(r -> r.name(EVENT_BUS_NAME));

		eventBridgeClient.putRule(r -> r
			.name(RULE_NAME)
			.eventBusName(EVENT_BUS_NAME)
			.eventPattern("{\"source\":[\"kete.keycloak\"]}")
			.state(RuleState.ENABLED));

		var sqsArn = "arn:aws:sqs:" + REGION + ":" + ACCOUNT_ID + ":" + VERIFY_QUEUE;
		eventBridgeClient.putTargets(r -> r
			.rule(RULE_NAME)
			.eventBusName(EVENT_BUS_NAME)
			.targets(Target.builder().id("1").arn(sqsArn).build()));
	}

	private String receiveVerificationMessage() {
		var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(VERIFY_QUEUE)).queueUrl();
		var messages = sqsClient.receiveMessage(r -> r.queueUrl(queueUrl).maxNumberOfMessages(1).waitTimeSeconds(5)).messages();
		if (messages.isEmpty()) {
			return "";
		}
		return messages.get(0).body();
	}

	private String getLocalStackBaseUrl() {
		return "http://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	private SqsClient buildVerificationSqsClient() {
		return SqsClient.builder()
			.region(Region.of(REGION))
			.credentialsProvider(AnonymousCredentialsProvider.create())
			.endpointOverride(URI.create(getLocalStackBaseUrl()))
			.httpClient(UrlConnectionHttpClient.create())
			.build();
	}

	private EventBridgeClient buildVerificationEventBridgeClient() {
		return EventBridgeClient.builder()
			.region(Region.of(REGION))
			.credentialsProvider(AnonymousCredentialsProvider.create())
			.endpointOverride(URI.create(getLocalStackBaseUrl()))
			.httpClient(UrlConnectionHttpClient.create())
			.build();
	}
}
