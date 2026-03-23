package io.github.fortunen.kete.integrationtests.awssnsdestination;

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
import io.github.fortunen.kete.destinations.awssns.AwsSnsDestination;
import io.github.fortunen.kete.destinations.awssns.AwsSnsDestinationConfig;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

public class TestBase {

	private static final String LOCALSTACK_IMAGE = "localstack/localstack";
	private static final int LOCALSTACK_PORT = 4566;

	protected static final String REGION = "us-east-1";
	protected static final String ACCOUNT_ID = "000000000000";

	protected GenericContainer<?> localstack;
	protected AwsSnsDestination destination;
	protected AwsSnsDestinationConfig config;
	protected SqsClient sqsClient;
	protected SnsClient snsClient;

	@BeforeEach
	void setUp() {
		destination = new AwsSnsDestination();
		config = new AwsSnsDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (snsClient != null) {
			try { snsClient.close(); } catch (Exception e) { /* ignore */ }
			snsClient = null;
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
			.withEnv("SERVICES", "sns,sqs");
		localstack.start();
		waitForLocalStackReady();
		sqsClient = buildVerificationSqsClient();
		snsClient = buildVerificationSnsClient();
	}

	@SuppressWarnings("resource")
	protected void startLocalStackWithTls(TlsMaterial tls) {
		var combined = new String(tls.getServerCertificatePemBytes(), StandardCharsets.UTF_8)
			+ new String(tls.getCaCertificatePemBytes(), StandardCharsets.UTF_8)
			+ new String(tls.getServerPrivateKeyPemBytes(), StandardCharsets.UTF_8);
		localstack = new GenericContainer<>(DockerImageName.parse(LOCALSTACK_IMAGE))
			.withExposedPorts(LOCALSTACK_PORT)
			.withEnv("SERVICES", "sns,sqs")
			.withEnv("CUSTOM_SSL_CERT_PATH", "/tmp/server.test.pem")
			.withCopyToContainer(Transferable.of(combined.getBytes(StandardCharsets.UTF_8)), "/tmp/server.test.pem");
		localstack.start();
		waitForLocalStackReady();
		sqsClient = buildVerificationSqsClient();
		snsClient = buildVerificationSnsClient();
	}

	protected String getLocalStackBaseUrl() {
		return "http://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	protected String getLocalStackTlsUrl() {
		return "https://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	protected String createTopic(String topic) {
		return snsClient.createTopic(r -> r.name(topic)).topicArn();
	}

	protected String createQueue(String queue) {
		return sqsClient.createQueue(r -> r.queueName(queue)).queueUrl();
	}

	protected String getQueueArn(String queueUrl) {
		return sqsClient.getQueueAttributes(r -> r.queueUrl(queueUrl).attributeNames(QueueAttributeName.QUEUE_ARN))
			.attributes().get(QueueAttributeName.QUEUE_ARN);
	}

	protected void subscribeQueueToTopic(String topicArn, String queueArn) {
		snsClient.subscribe(r -> r
			.topicArn(topicArn)
			.protocol("sqs")
			.endpoint(queueArn)
			.attributes(Map.of("RawMessageDelivery", "true")));
	}

	protected String receiveMessageFromQueue(String queue) {
		var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(queue)).queueUrl();
		var messages = sqsClient.receiveMessage(r -> r.queueUrl(queueUrl).maxNumberOfMessages(1).waitTimeSeconds(5)).messages();
		if (messages.isEmpty()) {
			return "";
		}
		return messages.get(0).body();
	}

	protected void configureDestination(String topic) {
		configureDestination(topic, Map.of());
	}

	protected void configureDestination(String topic, Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-sns");
		map.put("topic", topic);
		map.put("region", REGION);
		map.put("endpoint-url", getLocalStackBaseUrl());
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(String topic, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-sns");
		map.put("topic", topic);
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

	protected void configureDestinationWithMtls(String topic, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-sns");
		map.put("topic", topic);
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

	private SnsClient buildVerificationSnsClient() {
		return SnsClient.builder()
			.region(Region.of(REGION))
			.credentialsProvider(AnonymousCredentialsProvider.create())
			.endpointOverride(URI.create(getLocalStackBaseUrl()))
			.httpClient(UrlConnectionHttpClient.create())
			.build();
	}}
