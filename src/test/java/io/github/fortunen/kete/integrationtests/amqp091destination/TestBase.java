package io.github.fortunen.kete.integrationtests.amqp091destination;

import static org.awaitility.Awaitility.await;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.MountableFile;

import com.rabbitmq.client.ConnectionFactory;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.amqp091.Amqp091Destination;
import io.github.fortunen.kete.destinations.amqp091.Amqp091DestinationConfig;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int AMQP_TLS_PORT = 5671;

	protected RabbitMQContainer container;
	protected Amqp091Destination destination;
	protected Amqp091DestinationConfig config;

	@BeforeEach
	void setUp() {
		destination = new Amqp091Destination();
		config = new Amqp091DestinationConfig();
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected int getAmqpTlsPort() {
		return container.getMappedPort(AMQP_TLS_PORT);
	}

	protected RabbitMQContainer startRabbitMq() throws Exception {

		cleanUpContainer();

		container = new RabbitMQContainer("rabbitmq:3.13-management");
		container.start();

		waitForRabbitMqReady();

		return container;
	}

	protected RabbitMQContainer startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		return startRabbitMqWithTls(tls, false);
	}

	protected RabbitMQContainer startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		return startRabbitMqWithTls(tls, true);
	}

	private RabbitMQContainer startRabbitMqWithTls(TlsMaterial tls, boolean requireClientAuth) throws Exception {

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled (call withEnabled(true) when building TlsMaterial)");
		}

		if (tls.getCaCertificatePemFilePath() == null) {
			throw new IllegalStateException("CA certificate PEM file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		if (tls.getServerCertificatePemFilePath() == null) {
			throw new IllegalStateException("Server certificate PEM file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		if (tls.getServerPrivateKeyPkcs1PemFilePath() == null) {
			throw new IllegalStateException("Server private key PKCS#1 PEM file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		cleanUpContainer();

		// Debug: Print out the PEM file paths
		System.out.println("[TEST] CA Cert file: " + tls.getCaCertificatePemFilePath());
		System.out.println("[TEST] Server Cert file: " + tls.getServerCertificatePemFilePath());
		System.out.println("[TEST] Server Key file (PKCS#1): " + tls.getServerPrivateKeyPkcs1PemFilePath());

		// Create RabbitMQ configuration content for SSL
		// Note: RabbitMQ 3.13+ requires configuration file, not environment variables
		var sslVerify = requireClientAuth ? "verify_peer" : "verify_none";
		var failIfNoPeerCert = requireClientAuth ? "true" : "false";

		// Build config with basic SSL options
		var configBuilder = new StringBuilder();
		configBuilder.append("listeners.ssl.default = 5671\n");
		configBuilder.append("ssl_options.cacertfile = /etc/rabbitmq/ca_cert.pem\n");
		configBuilder.append("ssl_options.certfile = /etc/rabbitmq/rabbitmq_cert.pem\n");
		configBuilder.append("ssl_options.keyfile = /etc/rabbitmq/rabbitmq_key.pem\n");
		configBuilder.append("ssl_options.verify = ").append(sslVerify).append("\n");
		configBuilder.append("ssl_options.fail_if_no_peer_cert = ").append(failIfNoPeerCert).append("\n");

		configBuilder.append("\n"); // RabbitMQ config file needs empty line at end
		var configContent = configBuilder.toString();

		// Create temp config file
		var configFile = File.createTempFile("rabbitmq", ".conf");
		configFile.deleteOnExit();
		Files.writeString(configFile.toPath(), configContent);

		var rabbitContainer = new RabbitMQContainer("rabbitmq:3.13-management")
			.withCopyFileToContainer(MountableFile.forHostPath(tls.getServerPrivateKeyPkcs1PemFilePath()), "/etc/rabbitmq/rabbitmq_key.pem")
			.withCopyFileToContainer(MountableFile.forHostPath(tls.getServerCertificatePemFilePath()), "/etc/rabbitmq/rabbitmq_cert.pem")
			.withCopyFileToContainer(MountableFile.forHostPath(tls.getCaCertificatePemFilePath()), "/etc/rabbitmq/ca_cert.pem")
			.withRabbitMQConfig(MountableFile.forHostPath(configFile.getAbsolutePath()))
			.withExposedPorts(AMQP_TLS_PORT)
			.withLogConsumer(outputFrame -> System.out.println("[RABBITMQ] " + outputFrame.getUtf8String()));

		container = rabbitContainer;

		try {
			container.start();
		} catch (Exception e) {
			System.out.println("[TEST] Container failed to start. Logs:");
			System.out.println(container.getLogs());
			throw e;
		}

		// Note: For TLS mode, we skip the readiness probe since the plaintext port is not exposed.
		// The container.start() already waits for the container to be ready.

		return container;
	}

	protected void cleanUpContainer() {

		if (container != null) {
			try {
				container.stop();
			} catch (Exception exception) {
				// ignore
			}
		}

		container = null;
	}

	private void waitForRabbitMqReady() throws Exception {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {

				var factory = new ConnectionFactory();
				factory.setHost(container.getHost());
				factory.setPort(container.getAmqpPort());

				try (var connection = factory.newConnection()) {
					// Connection successful - broker is ready
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

	@AfterEach
	void tearDown() {
		cleanUp();
	}
}
