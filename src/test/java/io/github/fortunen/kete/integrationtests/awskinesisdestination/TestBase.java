package io.github.fortunen.kete.integrationtests.awskinesisdestination;

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
import io.github.fortunen.kete.destinations.awskinesis.AwsKinesisDestination;
import io.github.fortunen.kete.destinations.awskinesis.AwsKinesisDestinationConfig;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.StreamStatus;

public class TestBase {

	private static final String LOCALSTACK_IMAGE = "localstack/localstack";
	private static final int LOCALSTACK_PORT = 4566;

	protected static final String REGION = "us-east-1";

	protected GenericContainer<?> localstack;
	protected AwsKinesisDestination destination;
	protected AwsKinesisDestinationConfig config;
	protected KinesisClient kinesisClient;

	@BeforeEach
	void setUp() {
		destination = new AwsKinesisDestination();
		config = new AwsKinesisDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (kinesisClient != null) {
			try { kinesisClient.close(); } catch (Exception e) { /* ignore */ }
			kinesisClient = null;
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
			.withEnv("SERVICES", "kinesis");
		localstack.start();
		waitForLocalStackReady();
		kinesisClient = buildVerificationKinesisClient();
	}

	@SuppressWarnings("resource")
	protected void startLocalStackWithTls(TlsMaterial tls) {
		var combined = new String(tls.getServerCertificatePemBytes(), StandardCharsets.UTF_8)
			+ new String(tls.getCaCertificatePemBytes(), StandardCharsets.UTF_8)
			+ new String(tls.getServerPrivateKeyPemBytes(), StandardCharsets.UTF_8);
		localstack = new GenericContainer<>(DockerImageName.parse(LOCALSTACK_IMAGE))
			.withExposedPorts(LOCALSTACK_PORT)
			.withEnv("SERVICES", "kinesis")
			.withEnv("CUSTOM_SSL_CERT_PATH", "/tmp/server.test.pem")
			.withCopyToContainer(Transferable.of(combined.getBytes(StandardCharsets.UTF_8)), "/tmp/server.test.pem");
		localstack.start();
		waitForLocalStackReady();
		kinesisClient = buildVerificationKinesisClient();
	}

	protected String getLocalStackBaseUrl() {
		return "http://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	protected String getLocalStackTlsUrl() {
		return "https://" + "127.0.0.1" + ":" + localstack.getMappedPort(LOCALSTACK_PORT);
	}

	protected void createStream(String stream) {
		kinesisClient.createStream(r -> r.streamName(stream).shardCount(1));
		waitForStreamActive(stream);
	}

	protected String readRecordFromStream(String stream) {
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

	protected void configureDestination(String stream) {
		configureDestination(stream, Map.of());
	}

	protected void configureDestination(String stream, Map<String, Object> extras) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-kinesis");
		map.put("stream", stream);
		map.put("partition-key", "test-partition");
		map.put("region", REGION);
		map.put("endpoint-url", getLocalStackBaseUrl());
		map.putAll(extras);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected void configureDestinationWithTls(String stream, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-kinesis");
		map.put("stream", stream);
		map.put("partition-key", "test-partition");
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

	protected void configureDestinationWithMtls(String stream, TlsMaterial tls) {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-kinesis");
		map.put("stream", stream);
		map.put("partition-key", "test-partition");
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

	private void waitForStreamActive(String stream) {
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var status = kinesisClient.describeStream(r -> r.streamName(stream))
					.streamDescription().streamStatus();
				return status == StreamStatus.ACTIVE;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private KinesisClient buildVerificationKinesisClient() {
		return KinesisClient.builder()
			.region(Region.of(REGION))
			.credentialsProvider(AnonymousCredentialsProvider.create())
			.endpointOverride(URI.create(getLocalStackBaseUrl()))
			.httpClient(UrlConnectionHttpClient.create())
			.build();
	}}
