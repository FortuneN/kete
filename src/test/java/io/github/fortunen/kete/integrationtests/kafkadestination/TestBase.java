package io.github.fortunen.kete.integrationtests.kafkadestination;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.command.InspectContainerResponse;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.kafka.KafkaDestination;
import io.github.fortunen.kete.destinations.kafka.KafkaDestinationConfig;

import static org.awaitility.Awaitility.await;

public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	// Port 9095 is used for SSL - ports 9092 (PLAINTEXT), 9093 (BROKER), and 9094 (CONTROLLER)
	// are already used internally by KafkaContainer
	protected static final int KAFKA_SSL_PORT = 9095;
	protected static final int KAFKA_SASL_PORT = 9096;
	protected static final int KAFKA_PLAINTEXT_PORT = 9092;

	protected GenericContainer<?> container;
	protected KafkaDestination destination;
	protected KafkaDestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new KafkaDestination();
		config = new KafkaDestinationConfig();
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected GenericContainer<?> startKafka() throws Exception {
		cleanUpContainer();

		container = new KafkaContainer("apache/kafka:3.8.0");
		container.start();


		waitForKafkaReady();

		return container;
	}

	protected GenericContainer<?> startKafkaWithSasl(String username, String password) throws Exception {

		cleanUpContainer();

		SaslKafkaContainer kafkaContainer = new SaslKafkaContainer("apache/kafka:3.8.0", username, password);

		container = kafkaContainer;
		kafkaContainer.start();

		waitForKafkaReadyWithSasl(username, password);

		return container;
	}

	protected String getSaslBootstrapServers() {
		return container.getHost() + ":" + container.getMappedPort(KAFKA_SASL_PORT);
	}

	protected GenericContainer<?> startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		return startKafkaWithTls(tls, false);
	}

	protected GenericContainer<?> startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		return startKafkaWithTls(tls, true);
	}

	private GenericContainer<?> startKafkaWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled (call withEnabled(true) when building TlsMaterial)");
		}

		if (tls.getKeyStoreAndTrustStoreSSLContext() == null) {
			throw new IllegalStateException("SSL context is null - ensure TLS is properly configured");
		}

		if (tls.getKeyStoreFilePath() == null) {
			throw new IllegalStateException("Key store file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		if (tls.getTrustStoreFilePath() == null) {
			throw new IllegalStateException("Trust store file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		cleanUpContainer();

		// Kafka SSL configuration via environment variables
		// The apache/kafka image uses KRaft mode by default
		var sslClientAuth = requireClientAuth ? "required" : "none";

		// Use the built-in KafkaContainer with SSL listener support
		// We extend the default configuration by adding SSL environment variables
		SslKafkaContainer kafkaContainer = new SslKafkaContainer("apache/kafka:3.8.0", tls, sslClientAuth);

		container = kafkaContainer;
		kafkaContainer.start();

		// Wait for Kafka to be fully ready to accept connections
		waitForKafkaReady();

		// Log the advertised listeners for debugging
		System.out.println("[TEST] SSL Bootstrap Servers: " + getSslBootstrapServers());
		System.out.println("[TEST] Keystore password: " + tls.getKeyStorePassword());
		System.out.println("[TEST] Truststore password: " + tls.getTrustStorePassword());

		return container;
	}

	protected String getSslBootstrapServers() {
		return container.getHost() + ":" + container.getMappedPort(KAFKA_SSL_PORT);
	}

	protected String getPlaintextBootstrapServers() {
		return container.getHost() + ":" + container.getMappedPort(KAFKA_PLAINTEXT_PORT);
	}

	protected String getBootstrapServers() {
		if (container instanceof KafkaContainer) {
			return ((KafkaContainer) container).getBootstrapServers();
		} else if (container instanceof SslKafkaContainer) {
			return ((SslKafkaContainer) container).getBootstrapServers();
		}
		return getPlaintextBootstrapServers();
	}

	protected void cleanUpContainer() {

		if (container != null) {

			try {
				container.stop();
			} catch (Exception exception) {
				// ignore
			}

			container = null;
		}
	}

	private void waitForKafkaReady() throws Exception {
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var props = new Properties();
				props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
				props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
				try (var consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]>(props)) {
					consumer.listTopics(Duration.ofSeconds(10));
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void waitForKafkaReadyWithSasl(String username, String password) throws Exception {
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var props = new Properties();
				props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getSaslBootstrapServers());
				props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
				props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
				props.put("sasl.mechanism", "PLAIN");
				props.put("sasl.jaas.config", String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", username, password));
				try (var consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]>(props)) {
					consumer.listTopics(Duration.ofSeconds(10));
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		});
	}

	protected void cleanUp() {
		if (destination != null) {
			try {
				destination.close();
			} catch (Exception exception) {
				// ignore
			}
		}

		destination = null;

		cleanUpContainer();
	}

	protected static EventMessage createMessage(
		String eventId,
		String realm,
		boolean isAdminEvent,
		String eventType,
		String contentType,
		byte[] eventBody,
		String resourceType,
		String operationType
	) {
		return new EventMessage(
			realm != null ? realm : "",
			eventId != null ? eventId : "",
			eventBody != null ? eventBody : EMPTY_BYTES,
			eventType != null ? eventType : "",
			contentType != null ? contentType : "",
			resourceType != null ? resourceType : "",
			isAdminEvent ? Constants.ADMIN_EVENT : Constants.EVENT,
			operationType != null ? operationType : "",
			"SUCCESS"
		);
	}

	protected KafkaConsumer<String, byte[]> createConsumer(String topic) {
		var props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getPlaintextBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

		var consumer = new KafkaConsumer<String, byte[]>(props);
		consumer.subscribe(Collections.singletonList(topic));
		return consumer;
	}

	protected KafkaConsumer<String, byte[]> createSslConsumer(String topic, TlsMaterial tls) {
		var props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getSslBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
		// SSL configuration
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, tls.getTrustStoreFilePath());
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, tls.getTrustStorePassword() != null ? tls.getTrustStorePassword() : "");
		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, tls.getKeyStoreFilePath());
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, tls.getKeyStorePassword() != null ? tls.getKeyStorePassword() : "");
		props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, tls.getKeyPassword() != null ? tls.getKeyPassword() : "");

		var consumer = new KafkaConsumer<String, byte[]>(props);
		consumer.subscribe(Collections.singletonList(topic));
		return consumer;
	}

	protected KafkaConsumer<String, byte[]> createSaslConsumer(String topic, String username, String password) {
		var props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getSaslBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
		props.put("sasl.mechanism", "PLAIN");
		props.put("sasl.jaas.config", String.format(
			"org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
			username, password));

		var consumer = new KafkaConsumer<String, byte[]>(props);
		consumer.subscribe(Collections.singletonList(topic));
		return consumer;
	}

	protected String getHeaderValue(org.apache.kafka.common.header.Headers headers, String key) {
		Header header = headers.lastHeader(key);
		return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
	}

	@AfterEach
	void tearDown() {
		cleanUp();
	}

	private static class SslKafkaContainer extends KafkaContainer {

		private Path keyPasswordFile;
		private Path keystorePasswordFile;
		private Path truststorePasswordFile;

		SslKafkaContainer(String imageName, TlsMaterial tls, String sslClientAuth) throws IOException {
			super(imageName);

			// Create password files as expected by the apache/kafka Docker image
			// The image reads passwords from files, not env vars directly
			keyPasswordFile = Files.createTempFile("kafka-key-", ".creds");
			Files.writeString(keyPasswordFile, tls.getKeyPassword() != null ? tls.getKeyPassword() : "");

			keystorePasswordFile = Files.createTempFile("kafka-keystore-", ".creds");
			Files.writeString(keystorePasswordFile, tls.getKeyStorePassword() != null ? tls.getKeyStorePassword() : "");

			truststorePasswordFile = Files.createTempFile("kafka-truststore-", ".creds");
			Files.writeString(truststorePasswordFile, tls.getTrustStorePassword() != null ? tls.getTrustStorePassword() : "");

			// Add SSL listener configuration
			this.withExposedPorts(KAFKA_PLAINTEXT_PORT, KAFKA_SSL_PORT);

			// Mount the keystore, truststore, and password files
			// Use serverKeyStoreFilePath for the container (server-side TLS) - it contains only the server key
			this.withCopyFileToContainer(MountableFile.forHostPath(tls.getServerKeyStoreFilePath(), 0644), "/etc/kafka/secrets/keystore.jks");
			this.withCopyFileToContainer(MountableFile.forHostPath(tls.getTrustStoreFilePath(), 0644), "/etc/kafka/secrets/truststore.jks");
			this.withCopyFileToContainer(MountableFile.forHostPath(keyPasswordFile.toAbsolutePath().toString(), 0644), "/etc/kafka/secrets/key-password");
			this.withCopyFileToContainer(MountableFile.forHostPath(keystorePasswordFile.toAbsolutePath().toString(), 0644), "/etc/kafka/secrets/keystore-password");
			this.withCopyFileToContainer(MountableFile.forHostPath(truststorePasswordFile.toAbsolutePath().toString(), 0644), "/etc/kafka/secrets/truststore-password");

			// SSL configuration using FILENAME and CREDENTIALS pattern expected by apache/kafka image
			this.withEnv("KAFKA_SSL_KEYSTORE_FILENAME", "keystore.jks");
			this.withEnv("KAFKA_SSL_KEY_CREDENTIALS", "key-password");
			this.withEnv("KAFKA_SSL_KEYSTORE_CREDENTIALS", "keystore-password");
			this.withEnv("KAFKA_SSL_TRUSTSTORE_FILENAME", "truststore.jks");
			this.withEnv("KAFKA_SSL_TRUSTSTORE_CREDENTIALS", "truststore-password");

			// Client authentication setting (none for server-only TLS, required for mTLS)
			// This is the SERVER-SIDE enforcement - when set to "required", clients MUST present valid certificates
			this.withEnv("KAFKA_SSL_CLIENT_AUTH", sslClientAuth);

			// NOTE: Hostname verification is disabled for tests because we connect to localhost/container IPs
			// but the server certificate's CN/SAN may not match the dynamic port-mapped addresses.
			// The SERVER still enforces TLS/mTLS - this only affects client-side hostname verification.
			this.withEnv("KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM", "");

			// Increase startup timeout for CI environments
			this.withStartupTimeout(Duration.ofMinutes(10));

			// Log output for debugging
			this.withLogConsumer((OutputFrame outputFrame) -> System.out.print("[KAFKA] " + outputFrame.getUtf8String()));
		}

		@Override
		protected void containerIsStarting(InspectContainerResponse containerInfo) {
			// Get mapped ports for advertised listeners
			var mappedPlaintextPort = getMappedPort(KAFKA_PLAINTEXT_PORT);
			var mappedSslPort = getMappedPort(KAFKA_SSL_PORT);
			var hostName = getHost();
			var containerHostName = containerInfo.getConfig().getHostName();

			// Build the advertised listeners string including SSL
			var advertisedListeners = new ArrayList<String>();
			advertisedListeners.add("PLAINTEXT://" + hostName + ":" + mappedPlaintextPort);
			advertisedListeners.add("SSL://" + hostName + ":" + mappedSslPort);
			advertisedListeners.add("BROKER://" + containerHostName + ":9093");

			var kafkaAdvertisedListeners = String.join(",", advertisedListeners);

			// Override KAFKA_LISTENERS to include our SSL port
			var kafkaListeners = "PLAINTEXT://0.0.0.0:" + KAFKA_PLAINTEXT_PORT
				+ ",SSL://0.0.0.0:" + KAFKA_SSL_PORT
				+ ",BROKER://0.0.0.0:9093"
				+ ",CONTROLLER://0.0.0.0:9094";

			// Override KAFKA_LISTENER_SECURITY_PROTOCOL_MAP to include SSL
			var kafkaListenerSecurityProtocolMap = "BROKER:PLAINTEXT,PLAINTEXT:PLAINTEXT,SSL:SSL,CONTROLLER:PLAINTEXT";

			// Build the startup script
			var command = "#!/bin/bash\n";
			command += String.format("export KAFKA_LISTENERS='%s'\n", kafkaListeners);
			command += String.format("export KAFKA_LISTENER_SECURITY_PROTOCOL_MAP='%s'\n", kafkaListenerSecurityProtocolMap);
			command += String.format("export KAFKA_ADVERTISED_LISTENERS='%s'\n", kafkaAdvertisedListeners);
			command += "/etc/kafka/docker/run \n";

			copyFileToContainer(Transferable.of(command, 0777), "/tmp/testcontainers_start.sh");
		}

		@Override
		public void stop() {
			super.stop();
			// Clean up temp password files
			try {
				if (keyPasswordFile != null) Files.deleteIfExists(keyPasswordFile);
				if (keystorePasswordFile != null) Files.deleteIfExists(keystorePasswordFile);
				if (truststorePasswordFile != null) Files.deleteIfExists(truststorePasswordFile);
			} catch (IOException e) {
				// Ignore cleanup errors
			}
		}
	}

	private static class SaslKafkaContainer extends KafkaContainer {

		private Path jaasConfigFile;

		SaslKafkaContainer(String imageName, String username, String password) throws IOException {
			super(imageName);

			// Create JAAS config file for Kafka broker
			var jaasConfig = String.format(
				"KafkaServer {\n" +
				"    org.apache.kafka.common.security.plain.PlainLoginModule required\n" +
				"    username=\"%s\"\n" +
				"    password=\"%s\"\n" +
				"    user_%s=\"%s\";\n" +
				"};",
				username, password, username, password);

			jaasConfigFile = Files.createTempFile("kafka-jaas-", ".conf");
			Files.writeString(jaasConfigFile, jaasConfig);

			this.withExposedPorts(KAFKA_PLAINTEXT_PORT, KAFKA_SASL_PORT);
			this.withCopyFileToContainer(MountableFile.forHostPath(jaasConfigFile.toAbsolutePath().toString(), 0644), "/etc/kafka/kafka_server_jaas.conf");

			// SASL configuration
			this.withEnv("KAFKA_OPTS", "-Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf");
			this.withEnv("KAFKA_SASL_ENABLED_MECHANISMS", "PLAIN");
			this.withEnv("KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL", "PLAIN");

			this.withLogConsumer((OutputFrame outputFrame) -> System.out.print("[KAFKA-SASL] " + outputFrame.getUtf8String()));
		}

		public String getBootstrapServers() {
			return getHost() + ":" + getMappedPort(KAFKA_SASL_PORT);
		}

		@Override
		protected void containerIsStarting(InspectContainerResponse containerInfo) {
			var mappedPlaintextPort = getMappedPort(KAFKA_PLAINTEXT_PORT);
			var mappedSaslPort = getMappedPort(KAFKA_SASL_PORT);
			var hostName = getHost();
			var containerHostName = containerInfo.getConfig().getHostName();

			var advertisedListeners = new ArrayList<String>();
			advertisedListeners.add("PLAINTEXT://" + hostName + ":" + mappedPlaintextPort);
			advertisedListeners.add("SASL_PLAINTEXT://" + hostName + ":" + mappedSaslPort);
			advertisedListeners.add("BROKER://" + containerHostName + ":9093");

			var kafkaAdvertisedListeners = String.join(",", advertisedListeners);

			var kafkaListeners = "PLAINTEXT://0.0.0.0:" + KAFKA_PLAINTEXT_PORT
				+ ",SASL_PLAINTEXT://0.0.0.0:" + KAFKA_SASL_PORT
				+ ",BROKER://0.0.0.0:9093"
				+ ",CONTROLLER://0.0.0.0:9094";

			var kafkaListenerSecurityProtocolMap = "BROKER:PLAINTEXT,PLAINTEXT:PLAINTEXT,SASL_PLAINTEXT:SASL_PLAINTEXT,CONTROLLER:PLAINTEXT";

			var command = "#!/bin/bash\n";
			command += String.format("export KAFKA_LISTENERS='%s'\n", kafkaListeners);
			command += String.format("export KAFKA_LISTENER_SECURITY_PROTOCOL_MAP='%s'\n", kafkaListenerSecurityProtocolMap);
			command += String.format("export KAFKA_ADVERTISED_LISTENERS='%s'\n", kafkaAdvertisedListeners);
			command += "/etc/kafka/docker/run \n";

			copyFileToContainer(Transferable.of(command, 0777), "/tmp/testcontainers_start.sh");
		}

		@Override
		public void stop() {

			super.stop();

			try {
				if (jaasConfigFile != null) Files.deleteIfExists(jaasConfigFile);
			} catch (IOException e) {
				// Ignore cleanup errors
			}
		}
	}
}
