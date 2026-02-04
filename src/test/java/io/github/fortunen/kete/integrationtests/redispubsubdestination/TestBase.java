package io.github.fortunen.kete.integrationtests.redispubsubdestination;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.destinations.redispubsub.RedisPubSubDestination;
import io.github.fortunen.kete.destinations.redispubsub.RedisPubSubDestinationConfig;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SslOptions;
import io.lettuce.core.pubsub.RedisPubSubAdapter;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];
	protected static final int REDIS_PORT = 6379;
	protected static final int REDIS_TLS_PORT = 6380;

	protected GenericContainer<?> container;
	protected RedisPubSubDestination destination;
	protected RedisPubSubDestinationConfig config;
	protected TlsMaterial currentTls;

	@BeforeEach
	void setUp() {
		destination = new RedisPubSubDestination();
		config = new RedisPubSubDestinationConfig();
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected GenericContainer<?> startRedis() throws Exception {

		cleanUpContainer();

		container = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(REDIS_PORT);
		container.start();

		waitForRedisReady(REDIS_PORT, null);

		return container;
	}

	protected GenericContainer<?> startRedisWithPassword(String password) throws Exception {

		cleanUpContainer();

		container = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(REDIS_PORT)
			.withCommand("redis-server", "--requirepass", password);
		container.start();

		waitForRedisReady(REDIS_PORT, null);

		return container;
	}

	protected GenericContainer<?> startWithServerOnlyTLS(TlsMaterial tls) throws Exception {
		return startRedisWithTls(tls, false);
	}

	protected GenericContainer<?> startWithClientAndServerTLS(TlsMaterial tls) throws Exception {
		return startRedisWithTls(tls, true);
	}

	@SuppressWarnings("resource")
	private GenericContainer<?> startRedisWithTls(TlsMaterial tls, boolean requireClientCert) throws Exception {

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

		var tlsAuthClients = requireClientCert ? "yes" : "no";

		container = new GenericContainer<>(DockerImageName.parse("bitnami/redis:latest"))
			.withExposedPorts(REDIS_PORT)
			.withEnv("ALLOW_EMPTY_PASSWORD", "yes")
			.withEnv("REDIS_TLS_ENABLED", "yes")
			.withEnv("REDIS_TLS_CERT_FILE", "/certs/server.crt")
			.withEnv("REDIS_TLS_KEY_FILE", "/certs/server.key")
			.withEnv("REDIS_TLS_CA_FILE", "/certs/ca.crt")
			.withEnv("REDIS_TLS_AUTH_CLIENTS", tlsAuthClients)
			.withFileSystemBind(tls.getServerCertificatePemFilePath(), "/certs/server.crt", BindMode.READ_ONLY)
			.withFileSystemBind(tls.getServerPrivateKeyPemFilePath(), "/certs/server.key", BindMode.READ_ONLY)
			.withFileSystemBind(tls.getCaCertificatePemFilePath(), "/certs/ca.crt", BindMode.READ_ONLY)
			.withStartupTimeout(java.time.Duration.ofMinutes(10));

		container.start();

		waitForRedisReady(REDIS_PORT, tls);

		return container;
	}

	protected String getHost() {
		return container.getHost();
	}

	protected int getPort() {
		return container.getMappedPort(REDIS_PORT);
	}

	protected int getTlsPort() {
		// In bitnami/redis:latest (Redis 8.x), TLS and non-TLS share the same port
		return container.getMappedPort(REDIS_PORT);
	}

	private void waitForRedisReady(int port, TlsMaterial tls) throws Exception {
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var uriBuilder = RedisURI.builder()
					.withHost(container.getHost())
					.withPort(container.getMappedPort(port));

				if (tls != null && tls.isEnabled()) {
					uriBuilder.withSsl(true);
				}

				var uri = uriBuilder.build();
				var client = RedisClient.create(uri);

				if (tls != null && tls.isEnabled()) {
					var sslOptionsBuilder = SslOptions.builder()
						.jdkSslProvider()
						.trustManager(tls.getTrustManagerFactory());

					// Add key manager for mTLS (client certificate)
					if (tls.getKeyManagerFactory() != null) {
						sslOptionsBuilder.keyManager(tls.getKeyManagerFactory());
					}

					client.setOptions(ClientOptions.builder().sslOptions(sslOptionsBuilder.build()).build());
				}

				var connection = client.connect();
				var pong = connection.sync().ping();
				connection.close();
				client.close();
				return "PONG".equalsIgnoreCase(pong);
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

	protected static class MessageCollector extends RedisPubSubAdapter<String, String> {

		private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

		@Override
		public void message(String channel, String message) {
			messages.add(message);
		}

		public java.util.List<String> getMessages() {
			return messages;
		}
	}

	protected AutoCloseable createSubscriber(String channel, MessageCollector collector) throws Exception {
		return createSubscriber(channel, collector, null);
	}

	protected AutoCloseable createSubscriberWithTls(String channel, MessageCollector collector, TlsMaterial tls) throws Exception {
		return createSubscriber(channel, collector, tls);
	}

	private AutoCloseable createSubscriber(String channel, MessageCollector collector, TlsMaterial tls) throws Exception {

		// In bitnami/redis:latest (Redis 8.x), TLS and non-TLS share the same port
		var port = REDIS_PORT;

		var uriBuilder = RedisURI.builder()
			.withHost(container.getHost())
			.withPort(container.getMappedPort(port));

		if (tls != null && tls.isEnabled()) {
			uriBuilder.withSsl(true);
		}

		var uri = uriBuilder.build();
		var client = RedisClient.create(uri);

		if (tls != null && tls.isEnabled()) {
			var sslOptionsBuilder = SslOptions.builder()
				.jdkSslProvider()
				.trustManager(tls.getTrustManagerFactory());

			// Add key manager for mTLS (client certificate)
			if (tls.getKeyManagerFactory() != null) {
				sslOptionsBuilder.keyManager(tls.getKeyManagerFactory());
			}

			client.setOptions(ClientOptions.builder().sslOptions(sslOptionsBuilder.build()).build());
		}

		var connection = client.connectPubSub();
		connection.addListener(collector);
		connection.sync().subscribe(channel);

		return () -> {
			try {
				connection.close();
				client.close();
			} catch (Exception ignored) {
			}
		};
	}
}
