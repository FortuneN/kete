package io.github.fortunen.kete.integrationtests.pulsardestination;

import static org.awaitility.Awaitility.await;

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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.pulsar.PulsarDestination;
import io.github.fortunen.kete.destinations.pulsar.PulsarDestinationConfig;

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
		mapConfig.setProperty(Constants.KIND, "pulsar");
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected GenericContainer<?> startPulsar() throws Exception {

		cleanUpContainer();

		container = new GenericContainer<>(DockerImageName.parse("apachepulsar/pulsar:3.3.2"))
			.withExposedPorts(PULSAR_PORT, PULSAR_HTTP_PORT)
			.withCommand("bin/pulsar", "standalone", "--no-functions-worker");

		container.start();
		waitForBrokerHealthy();

		return container;
	}

	private void waitForBrokerHealthy() {

		// Wait for broker healthcheck first

		await().atMost(Duration.ofMinutes(10)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var result = container.execInContainer("curl", "-sf", "http://localhost:8080/admin/v2/brokers/healthcheck");
				return result.getExitCode() == 0;
			} catch (Exception e) {
				return false;
			}
		});

		// Then wait for the default namespace to be ready

		await().atMost(Duration.ofMinutes(10)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var result = container.execInContainer("curl", "-sf", "http://localhost:8080/admin/v2/namespaces/public/default");
				return result.getExitCode() == 0;
			} catch (Exception e) {
				return false;
			}
		});

		// Wait for BookKeeper to be ready by verifying topic creation works (this is the actual readiness check)

		await().atMost(Duration.ofMinutes(10)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var result = container.execInContainer(
					"bin/pulsar-admin", "topics", "create", "persistent://public/default/readiness-check"
				);
				if (result.getExitCode() == 0) {
					// Clean up the test topic
					container.execInContainer("bin/pulsar-admin", "topics", "delete", "persistent://public/default/readiness-check");
					return true;
				}
				return false;
			} catch (Exception e) {
				return false;
			}
		});
	}

	protected GenericContainer<?> startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		return startPulsarWithTls(tls, false);
	}

	protected GenericContainer<?> startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		return startPulsarWithTls(tls, true);
	}

	private GenericContainer<?> startPulsarWithTls(TlsMaterial tls, boolean requireClientCert) throws Exception {

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled");
		}

		cleanUpContainer();
		currentTls = tls;

		// Use a startup script that appends TLS settings to the default standalone.conf
		// This preserves all essential standalone defaults while adding TLS configuration
		var startupScript = createStartupScript(requireClientCert);

		container = new GenericContainer<>(DockerImageName.parse("apachepulsar/pulsar:3.3.2"))
			.withExposedPorts(PULSAR_PORT, PULSAR_TLS_PORT, PULSAR_HTTP_PORT)
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getServerCertificatePemFilePath())), 0777), "/pulsar/server-cert.pem")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getServerPrivateKeyPemFilePath())), 0777), "/pulsar/server-key.pem")
			.withCopyToContainer(Transferable.of(Files.readAllBytes(Path.of(tls.getCaCertificatePemFilePath())), 0777), "/pulsar/ca-cert.pem")
			.withCommand("/bin/bash", "-c", startupScript);

		container.start();
		waitForBrokerHealthy();

		return container;
	}

	private String createStartupScript(boolean requireClientCert) {
		// Append TLS settings to the default standalone.conf, then start Pulsar
		// This preserves all essential standalone defaults while adding TLS configuration
		return """
			cat >> /pulsar/conf/standalone.conf << 'EOF'
			# TLS Configuration (PEM-based)
			brokerServicePortTls=%d
			webServicePortTls=8443
			tlsEnabled=true
			tlsCertificateFilePath=/pulsar/server-cert.pem
			tlsKeyFilePath=/pulsar/server-key.pem
			tlsTrustCertsFilePath=/pulsar/ca-cert.pem
			tlsRequireTrustedClientCertOnConnect=%s
			brokerClientTlsEnabled=false
			functionsWorkerEnabled=false
			EOF
			bin/pulsar standalone --no-functions-worker
			""".formatted(PULSAR_TLS_PORT, requireClientCert);
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

	@SuppressWarnings("deprecation")
	private Consumer<byte[]> createSubscriber(String topic, TlsMaterial tls) throws Exception {

		var clientBuilder = PulsarClient.builder()
			.connectionTimeout(10, TimeUnit.SECONDS)
			.operationTimeout(30, TimeUnit.SECONDS);

		if (tls != null && tls.isEnabled()) {
			clientBuilder.serviceUrl(getPulsarTlsUrl())
				.tlsTrustCertsFilePath(tls.getCaCertificatePemFilePath())
				.enableTls(true)
				.allowTlsInsecureConnection(false);

			// Add client auth if keystore provided (for mTLS)
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
