package io.github.fortunen.kete.integrationtests.awseventbridgedestination;

import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.awseventbridge.AwsEventBridgeDestination;
import io.github.fortunen.kete.destinations.awseventbridge.AwsEventBridgeDestinationConfig;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.RuleState;
import software.amazon.awssdk.services.eventbridge.model.Target;
import software.amazon.awssdk.services.sqs.SqsClient;

public class TestBase {

	private static final String LOCALSTACK_IMAGE = "localstack/localstack";
	private static final int LOCALSTACK_PORT = 4566;

	protected static final String REGION = "us-east-1";
	protected static final String ACCOUNT_ID = "000000000000";
	protected static final String EVENT_BUS_NAME = "test-bus";
	protected static final String VERIFY_QUEUE = "verify-queue";
	protected static final String RULE_NAME = "catch-all";

	protected GenericContainer<?> localstack;
	protected AwsEventBridgeDestination destination;
	protected AwsEventBridgeDestinationConfig config;
	protected SqsClient sqsClient;
	protected EventBridgeClient eventBridgeClient;

	@BeforeEach
	void setUp() {
		destination = new AwsEventBridgeDestination();
		config = new AwsEventBridgeDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (eventBridgeClient != null) {
			try { eventBridgeClient.close(); } catch (Exception e) { /* ignore */ }
			eventBridgeClient = null;
		}
		if (sqsClient != null) {
			try { sqsClient.close(); } catch (Exception e) { /* ignore */ }
			sqsClient = null;
		}
		if (localstack != null) {
			try { localstack.stop(); } catch (Exception e) { /* ignore */ }
			localstack = null;
		}
	}

	@SuppressWarnings("resource")
	protected void startLocalStack() {
		localstack = new GenericContainer<>(DockerImageName.parse(LOCALSTACK_IMAGE))
			.withExposedPorts(LOCALSTACK_PORT)
			.withEnv("SERVICES", "events,sqs");
		localstack.start();
		waitForLocalStackReady();
		sqsClient = buildVerificationSqsClient();
		eventBridgeClient = buildVerificationEventBridgeClient();
	}

	@SuppressWarnings("resource")
	protected void startLocalStackWithTls(TlsMaterial tls) {
		var combined = new String(tls.getServerCertificatePemBytes(), StandardCharsets.UTF_8)
			+ new String(tls.getCaCertificatePemBytes(), StandardCharsets.UTF_8)
			+ new String(tls.getServerPrivateKeyPemBytes(), StandardCharsets.UTF_8);
		localstack = new GenericContainer<>(DockerImageName.parse(LOCALSTACK_IMAGE))
			.withExposedPorts(LOCALSTACK_PORT)
			.withEnv("SERVICES", "events,sqs")
			.withEnv("CUSTOM_SSL_CERT_PATH", "/tmp/server.test.pem")
			.withCopyToContainer(Transferable.of(combined.getBytes(StandardCharsets.UTF_8)),
				"/tmp/server.test.pem");
		localstack.start();
		waitForLocalStackReady();
		sqsClient = buildVerificationSqsClient();
		eventBridgeClient = buildVerificationEventBridgeClient();
	}

	protected String getLocalStackBaseUrl() {
		return "http://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	protected String getLocalStackTlsUrl() {
		return "https://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	protected void createEventBusAndVerificationQueue() {
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

	protected String receiveVerificationMessage() {
		var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(VERIFY_QUEUE)).queueUrl();
		var messages = sqsClient.receiveMessage(r -> r.queueUrl(queueUrl).maxNumberOfMessages(1).waitTimeSeconds(5)).messages();
		if (messages.isEmpty()) {
			return "";
		}
		return messages.get(0).body();
	}

	protected void configureDestination() {
		configureDestination(Map.of());
	}

	protected void configureDestination(Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-eventbridge");
		map.put("event-bus", EVENT_BUS_NAME);
		map.put("source", "kete.keycloak");
		map.put("detail-type", "KeycloakEvent");
		map.put("region", REGION);
		map.put("endpoint-url", getLocalStackBaseUrl());
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-eventbridge");
		map.put("event-bus", EVENT_BUS_NAME);
		map.put("source", "kete.keycloak");
		map.put("detail-type", "KeycloakEvent");
		map.put("region", REGION);
		map.put("endpoint-url", getLocalStackTlsUrl());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithMtls(TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-eventbridge");
		map.put("event-bus", EVENT_BUS_NAME);
		map.put("source", "kete.keycloak");
		map.put("detail-type", "KeycloakEvent");
		map.put("region", REGION);
		map.put("endpoint-url", getLocalStackTlsUrl());
		map.put("tls.enabled", true);
		map.put("tls.trust-store.loader.kind", "jks-file-path");
		map.put("tls.trust-store.loader.path", tls.getTrustStoreFilePath());
		map.put("tls.trust-store.password", tls.getTrustStorePassword());
		map.put("tls.key-store.loader.kind", "jks-file-path");
		map.put("tls.key-store.loader.path", tls.getKeyStoreFilePath());
		map.put("tls.key-store.password", tls.getKeyStorePassword());
		map.put("tls.key-store.key-password", tls.getKeyPassword());
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
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
	}}
