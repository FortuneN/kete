package io.github.fortunen.kete.integrationtests.pulsardestination;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.pulsar.PulsarDestination;
import io.github.fortunen.kete.destinations.pulsar.PulsarDestinationConfig;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int PULSAR_PORT = 6650;
	protected static final int PULSAR_TLS_PORT = 6651;
	protected static final int PULSAR_HTTP_PORT = 8080;

	protected GenericContainer<?> container;
	protected PulsarDestination destination;
	protected PulsarDestinationConfig config;
	protected TlsMaterial currentTls;

	@BeforeEach
	void setUp() {
		destination = new PulsarDestination();
		config = new PulsarDestinationConfig();
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected GenericContainer<?> startPulsar() throws Exception {

		cleanUpContainer();

		container = new GenericContainer<>(DockerImageName.parse("apachepulsar/pulsar:3.3.2"))
			.withExposedPorts(PULSAR_PORT, PULSAR_HTTP_PORT)
			.withCommand("/bin/bash", "-c", "bin/pulsar standalone --no-functions-worker -nss")
			.waitingFor(Wait.forLogMessage(".*messaging service is ready.*", 1))
			.withStartupTimeout(Duration.ofMinutes(10));

		container.start();

		return container;
	}

	protected GenericContainer<?> startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		return startPulsarWithTls(tls, false);
	}

	protected GenericContainer<?> startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		return startPulsarWithTls(tls, true);
	}

	@SuppressWarnings("resource")
	private GenericContainer<?> startPulsarWithTls(TlsMaterial tls, boolean requireClientCert) throws Exception {

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled");
		}

		cleanUpContainer();
		currentTls = tls;

		// Create temporary directory for mounted files
		var tempDir = Files.createTempDirectory("pulsar-test-");

		// Copy certificate files to temp directory
		var hostKeyStore = tempDir.resolve("keystore.jks");
		var hostTrustStore = tempDir.resolve("truststore.jks");
		var hostBrokerConf = tempDir.resolve("broker.conf");

		Files.copy(Path.of(tls.getServerKeyStoreFilePath()), hostKeyStore);
		Files.copy(Path.of(tls.getTrustStoreFilePath()), hostTrustStore);

		// Create broker.conf with TLS settings
		var brokerConfig = createBrokerConfig(tls.getKeyStorePassword(), tls.getTrustStorePassword(), requireClientCert);
		Files.writeString(hostBrokerConf, brokerConfig);

		container = new GenericContainer<>(DockerImageName.parse("apachepulsar/pulsar:3.3.2"))
			.withExposedPorts(PULSAR_PORT, PULSAR_TLS_PORT, PULSAR_HTTP_PORT)
			.withFileSystemBind(hostKeyStore.toString(), "/pulsar/keystore.jks", BindMode.READ_ONLY)
			.withFileSystemBind(hostTrustStore.toString(), "/pulsar/truststore.jks", BindMode.READ_ONLY)
			.withFileSystemBind(hostBrokerConf.toString(), "/pulsar/conf/broker.conf", BindMode.READ_ONLY)
			.withCommand("/bin/bash", "-c", "bin/pulsar standalone --no-functions-worker -nss --config /pulsar/conf/broker.conf")
			.waitingFor(Wait.forLogMessage(".*messaging service is ready.*", 1))
			.withStartupTimeout(Duration.ofMinutes(10));

		container.start();

		return container;
	}

	private String createBrokerConfig(String keyStorePassword, String trustStorePassword, boolean requireClientCert) {
		var clientAuthMode = requireClientCert ? "REQUIRE" : "OPTIONAL";
		return """
			# Basic Configuration
			clusterName=standalone

			# TLS Configuration
			brokerServicePortTls=%d
			webServicePortTls=8443
			tlsEnabled=true
			tlsCertificateFilePath=/pulsar/keystore.jks
			tlsKeyFilePath=/pulsar/keystore.jks
			tlsKeyStoreType=JKS
			tlsKeyStorePassword=%s
			tlsTrustStoreType=JKS
			tlsTrustStorePath=/pulsar/truststore.jks
			tlsTrustStorePassword=%s
			tlsRequireTrustedClientCertOnConnect=%s
			brokerClientTlsEnabled=true
			""".formatted(PULSAR_TLS_PORT, keyStorePassword, trustStorePassword, clientAuthMode);
	}

	protected String getHost() {
		return container.getHost();
	}

	protected int getPort() {
		return container.getMappedPort(PULSAR_PORT);
	}

	protected String getPulsarUrl() {
		return "pulsar://" + getHost() + ":" + getPort();
	}

	protected String getPulsarTlsUrl() {
		return "pulsar+ssl://" + getHost() + ":" + container.getMappedPort(PULSAR_TLS_PORT);
	}

	protected Consumer<byte[]> createSubscriber(String topic) throws Exception {
		return createSubscriber(topic, null);
	}

	protected Consumer<byte[]> createSubscriberWithTls(String topic, TlsMaterial tls) throws Exception {
		return createSubscriber(topic, tls);
	}

	private Consumer<byte[]> createSubscriber(String topic, TlsMaterial tls) throws Exception {

		var clientBuilder = PulsarClient.builder()
			.connectionTimeout(10, TimeUnit.SECONDS)
			.operationTimeout(30, TimeUnit.SECONDS);

		if (tls != null && tls.isEnabled()) {
			clientBuilder.serviceUrl(getPulsarTlsUrl())
				.tlsTrustCertsFilePath(tls.getTrustStoreFilePath())
				.tlsTrustStorePassword(tls.getTrustStorePassword())
				.tlsTrustStoreType("JKS")
				.enableTls(true)
				.allowTlsInsecureConnection(false);

			// Add client auth if keystore provided
			if (tls.getKeyStoreFilePath() != null) {
				clientBuilder.authentication(
					"org.apache.pulsar.client.impl.auth.AuthenticationTls",
					"tlsCertFile:" + tls.getClientCertificatePemFilePath() + ",tlsKeyFile:" + tls.getClientPrivateKeyPemFilePath()
				);
			}
		} else {
			clientBuilder.serviceUrl(getPulsarUrl());
		}

		var client = clientBuilder.build();

		return client.newConsumer()
			.topic(topic)
			.subscriptionName("test-subscription")
			.subscribe();
	}

	protected List<byte[]> receiveMessages(Consumer<byte[]> consumer, int count, int timeoutSeconds) throws Exception {
		var messages = new ArrayList<byte[]>();
		var deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);

		while (messages.size() < count && System.currentTimeMillis() < deadline) {
			var msg = consumer.receive((int) (deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
			if (msg != null) {
				messages.add(msg.getData());
				consumer.acknowledge(msg);
			}
		}

		return messages;
	}

	protected EventMessage createMessage(String eventId, String realm, boolean isAdminEvent,
			String eventType, String contentType, byte[] eventBody,
			String resourceType, String operationType) {

		return new EventMessage(
			realm != null ? realm : "",
			eventId != null ? eventId : "",
			eventBody != null ? eventBody : EMPTY_BYTES,
			eventType != null ? eventType : "",
			contentType != null ? contentType : "",
			resourceType != null ? resourceType : "",
			isAdminEvent ? Constants.ADMIN_EVENT : Constants.EVENT,
			operationType != null ? operationType : "",
			null
		);
	}

	protected void cleanUpContainer() {
		if (container != null) {
			try {
				container.stop();
			} catch (Exception e) {
				// ignore
			}
			container = null;
		}
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try {
				destination.close();
			} catch (Exception e) {
				// ignore
			}
		}

		cleanUpContainer();
	}
}
