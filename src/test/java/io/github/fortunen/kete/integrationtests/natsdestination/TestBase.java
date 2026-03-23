package io.github.fortunen.kete.integrationtests.natsdestination;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.nats.NatsDestination;
import io.github.fortunen.kete.destinations.nats.NatsDestinationConfig;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

import io.nats.client.Message;

public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int NATS_PORT = 4222;
	protected static final int NATS_MONITORING_PORT = 8222;

	protected GenericContainer<?> container;
	protected NatsDestination destination;
	protected NatsDestinationConfig config;
	protected TlsMaterial currentTls;

	@BeforeEach
	void setUp() {
		destination = new NatsDestination();
		config = new NatsDestinationConfig();
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		mapConfig.setProperty(Constants.KIND, "nats");
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	@SuppressWarnings("resource")
	protected GenericContainer<?> startNats() throws Exception {

		cleanUpContainer();

		container = new GenericContainer<>(DockerImageName.parse("nats:2.10-alpine"))
			.withExposedPorts(NATS_PORT, NATS_MONITORING_PORT)
			.withCommand("--http_port", "8222");
		container.start();

		waitForNatsReady();

		return container;
	}

	protected GenericContainer<?> startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		return startNatsWithTls(tls, false);
	}

	protected GenericContainer<?> startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		return startNatsWithTls(tls, true);
	}

	@SuppressWarnings("resource")
	private GenericContainer<?> startNatsWithTls(TlsMaterial tls, boolean requireClientCert) throws Exception {

		if (tls == null) {
			throw new IllegalArgumentException("TLS material cannot be null");
		}

		if (!tls.isEnabled()) {
			throw new IllegalArgumentException("TLS must be enabled");
		}

		if (tls.getServerCertificatePemFilePath() == null) {
			throw new IllegalStateException("Server certificate PEM file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		if (tls.getServerPrivateKeyPemFilePath() == null) {
			throw new IllegalStateException("Server private key PEM file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		if (tls.getCaCertificatePemFilePath() == null) {
			throw new IllegalStateException("CA certificate PEM file path is null - ensure TLS material is built with withWriteFiles(true)");
		}

		cleanUpContainer();
		currentTls = tls;

		var tlsVerifyOption = requireClientCert ? "true" : "false";

		container = new GenericContainer<>(DockerImageName.parse("nats:2.10-alpine"))
			.withExposedPorts(NATS_PORT, NATS_MONITORING_PORT)
			.withCopyToContainer(Transferable.of(tls.getServerCertificatePemBytes(), 0777), "/certs/server.crt")
			.withCopyToContainer(Transferable.of(tls.getServerPrivateKeyPemBytes(), 0777), "/certs/server.key")
			.withCopyToContainer(Transferable.of(tls.getCaCertificatePemBytes(), 0777), "/certs/ca.crt")
			.withCommand(
				"--http_port", "8222",
				"--tls",
				"--tlscert=/certs/server.crt",
				"--tlskey=/certs/server.key",
				"--tlsverify=" + tlsVerifyOption,
				"--tlscacert=/certs/ca.crt"
			);

		container.start();

		waitForNatsReady();

		return container;
	}

	protected String getHost() {
		return "127.0.0.1";
	}

	protected int getPort() {
		return container.getMappedPort(NATS_PORT);
	}

	protected String getNatsUrl() {
		return "nats://" + getHost() + ":" + getPort();
	}

	protected String getNatsTlsUrl() {
		return "tls://" + getHost() + ":" + getPort();
	}

	private void waitForNatsReady() throws Exception {
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var url = "http://" + "127.0.0.1" + ":" + container.getMappedPort(NATS_MONITORING_PORT) + "/varz";
				var connection = new URI(url).toURL().openConnection();
				connection.setConnectTimeout(1000);
				connection.setReadTimeout(1000);
				connection.connect();
				var responseCode = ((HttpURLConnection) connection).getResponseCode();
				return responseCode == 200;
			} catch (Exception e) {
				return false;
			}
		});
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

	@AfterEach
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

	protected static class MessageCollector {

		private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

		public void onMessage(Message msg) {
			messages.add(new String(msg.getData()));
		}

		public List<String> getMessages() {
			return messages;
		}
	}

	protected AutoCloseable createSubscriber(String subject, MessageCollector collector) throws Exception {
		return createSubscriber(subject, collector, null);
	}

	protected AutoCloseable createSubscriberWithTls(String subject, MessageCollector collector, TlsMaterial tls) throws Exception {
		return createSubscriber(subject, collector, tls);
	}

	private AutoCloseable createSubscriber(String subject, MessageCollector collector, TlsMaterial tls) throws Exception {

		var optionsBuilder = new Options.Builder()
			.server(tls != null && tls.isEnabled() ? getNatsTlsUrl() : getNatsUrl());

		if (tls != null && tls.isEnabled()) {
			optionsBuilder.sslContext(tls.getServerKeyStoreSSLContext());
		}

		var options = optionsBuilder.build();
		var connection = Nats.connect(options);

		var dispatcher = connection.createDispatcher(collector::onMessage);
		dispatcher.subscribe(subject);

		return () -> {
			try {
				dispatcher.unsubscribe(subject);
				connection.close();
			} catch (Exception ignored) {
			}
		};
	}
}
