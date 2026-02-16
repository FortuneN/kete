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
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.StreamStatus;

class AwsKinesisDestinationE2ETests extends EndToEndTestBase {

	private static final String LOCALSTACK_IMAGE = "localstack/localstack";
	private static final int LOCALSTACK_PORT = 4566;
	private static final String REGION = "us-east-1";
	private static final String STREAM_NAME = "test-stream";

	private GenericContainer<?> localstack;
	private KinesisClient kinesisClient;

	@AfterEach
	void tearDown() {
		if (kinesisClient != null) {
			try { kinesisClient.close(); } catch (Exception e) { /* ignore */ }
			kinesisClient = null;
		}
		if (localstack != null) {
			localstack.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToAwsKinesis() throws Exception {

		// arrange

		localstack = new GenericContainer<>(DockerImageName.parse(LOCALSTACK_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("localstack")
			.withExposedPorts(LOCALSTACK_PORT)
			.withEnv("SERVICES", "kinesis");
		localstack.start();

		waitForLocalStackReady();
		kinesisClient = buildVerificationKinesisClient();
		createStream(STREAM_NAME);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.kinesis-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.kinesis-test.destination.kind", "aws-kinesis");
		envVars.put("kete.routes.kinesis-test.destination.stream", STREAM_NAME);
		envVars.put("kete.routes.kinesis-test.destination.partition-key", "test-partition");
		envVars.put("kete.routes.kinesis-test.destination.region", REGION);
		envVars.put("kete.routes.kinesis-test.destination.endpoint-url", "http://localstack:" + LOCALSTACK_PORT);
		envVars.put("kete.routes.kinesis-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — poll Kinesis for the record

				await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
					var record = readRecordFromStream(STREAM_NAME);
					assertThat(record).isNotEmpty();
					assertThat(record).satisfiesAnyOf(
						r -> assertThat(r).contains("\"type\""),
						r -> assertThat(r).contains("\"operationType\"")
					);
					assertThat(record).satisfiesAnyOf(
						r -> assertThat(r).contains("\"realmName\""),
						r -> assertThat(r).contains("\"realmId\"")
					);
					assertThat(record).contains(TEST_REALM);
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

	private void createStream(String stream) {
		kinesisClient.createStream(r -> r.streamName(stream).shardCount(1));
		waitForStreamActive(stream);
	}

	private String readRecordFromStream(String stream) {
		var shardIterator = kinesisClient.getShardIterator(r -> r
			.streamName(stream)
			.shardId("shardId-000000000000")
			.shardIteratorType(ShardIteratorType.TRIM_HORIZON))
			.shardIterator();
		var records = kinesisClient.getRecords(r -> r.shardIterator(shardIterator).limit(1)).records();
		if (records.isEmpty()) {
			return "";
		}
		return records.get(0).data().asUtf8String();
	}

	private void waitForStreamActive(String stream) {
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var status = kinesisClient.describeStream(r -> r.streamName(stream))
					.streamDescription().streamStatus();
				return status == StreamStatus.ACTIVE;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private String getLocalStackBaseUrl() {
		return "http://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	private KinesisClient buildVerificationKinesisClient() {
		return KinesisClient.builder()
			.region(Region.of(REGION))
			.credentialsProvider(AnonymousCredentialsProvider.create())
			.endpointOverride(URI.create(getLocalStackBaseUrl()))
			.httpClient(UrlConnectionHttpClient.create())
			.build();
	}
}
