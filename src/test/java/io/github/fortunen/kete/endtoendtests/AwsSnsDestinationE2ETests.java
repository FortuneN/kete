package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

class AwsSnsDestinationE2ETests extends EndToEndTestBase {

	private static final String LOCALSTACK_IMAGE = "localstack/localstack";
	private static final int LOCALSTACK_PORT = 4566;
	private static final String REGION = "us-east-1";
	private static final String ACCOUNT_ID = "000000000000";
	private static final String TOPIC_NAME = "test-topic";

	private GenericContainer<?> localstack;
	private SqsClient sqsClient;
	private SnsClient snsClient;

	@AfterEach
	void tearDown() {
		if (snsClient != null) {
			try { snsClient.close(); } catch (Exception e) { /* ignore */ }
			snsClient = null;
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
	void shouldForwardLoginEventToAwsSns() throws Exception {

		// arrange

		localstack = new GenericContainer<>(DockerImageName.parse(LOCALSTACK_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("localstack")
			.withExposedPorts(LOCALSTACK_PORT)
			.withEnv("SERVICES", "sns,sqs");
		localstack.start();

		waitForLocalStackReady();
		sqsClient = buildVerificationSqsClient();
		snsClient = buildVerificationSnsClient();

		// create SNS topic

		var topicArn = createTopic(TOPIC_NAME);

		// create SQS verification queue and subscribe it to the topic

		var verificationQueue = "verification-queue";
		createQueue(verificationQueue);
		var queueArn = getQueueArn(verificationQueue);
		subscribeQueueToTopic(topicArn, queueArn);

		// configure Keycloak

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.sns-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.sns-test.destination.kind", "aws-sns");
		envVars.put("kete.routes.sns-test.destination.topic", TOPIC_NAME);
		envVars.put("kete.routes.sns-test.destination.region", REGION);
		envVars.put("kete.routes.sns-test.destination.account-id", ACCOUNT_ID);
		envVars.put("kete.routes.sns-test.destination.endpoint-url", "http://localstack:" + LOCALSTACK_PORT);
		envVars.put("kete.routes.sns-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — poll SQS verification queue for the message sent via SNS

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
					var body = receiveMessageFromQueue(verificationQueue);
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
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
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

	private String createTopic(String topic) {
		return snsClient.createTopic(r -> r.name(topic)).topicArn();
	}

	private void createQueue(String queue) {
		sqsClient.createQueue(r -> r.queueName(queue));
	}

	private String getQueueArn(String queue) {
		var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(queue)).queueUrl();
		return sqsClient.getQueueAttributes(r -> r.queueUrl(queueUrl).attributeNames(QueueAttributeName.QUEUE_ARN))
			.attributes().get(QueueAttributeName.QUEUE_ARN);
	}

	private void subscribeQueueToTopic(String topicArn, String queueArn) {
		snsClient.subscribe(r -> r
			.topicArn(topicArn)
			.protocol("sqs")
			.endpoint(queueArn)
			.attributes(Map.of("RawMessageDelivery", "true")));
	}

	private String receiveMessageFromQueue(String queue) {
		var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(queue)).queueUrl();
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

	private SnsClient buildVerificationSnsClient() {
		return SnsClient.builder()
			.region(Region.of(REGION))
			.credentialsProvider(AnonymousCredentialsProvider.create())
			.endpointOverride(URI.create(getLocalStackBaseUrl()))
			.httpClient(UrlConnectionHttpClient.create())
			.build();
	}
}
