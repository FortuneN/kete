package io.github.fortunen.kete.integrationtests.natsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationPooledObjectFactory;
import io.github.fortunen.kete.destinations.nats.NatsDestination;
import io.github.fortunen.kete.utils.DestinationUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

/**
 * Repro scenario for the issue behind PR #31: a NATS connection that reaches the
 * terminal CLOSED state (repeated authorization failures during reconnect) must not
 * leave the pool serving a dead sender. isHealthy() plus test-on-borrow culls the
 * instance and publishing resumes without any restart.
 */
public class isHealthyTests extends TestBase {

	private static final String USERNAME = "test-user";
	private static final String GOOD_PASSWORD = "good-password";
	private static final String ROTATED_PASSWORD = "rotated-password";

	private GenericContainer<?> authContainer;
	private GenericObjectPool<Destination<?>> pool;

	@AfterEach
	void cleanUpPoolAndContainer() {

		if (pool != null) {
			try { pool.close(); } catch (Exception exception) { /* ignore */ }
			pool = null;
		}

		if (authContainer != null) {
			try { authContainer.stop(); } catch (Exception exception) { /* ignore */ }
			authContainer = null;
		}
	}

	private static int findFreePort() throws Exception {
		try (var socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	@SuppressWarnings("resource")
	private void startAuthNats(int hostPort, String password) {

		authContainer = new GenericContainer<>(DockerImageName.parse("nats:2.10-alpine"))
			.withCommand("--user", USERNAME, "--pass", password, "--http_port", "8222")
			.withExposedPorts(NATS_MONITORING_PORT)
			.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig()
				.withPortBindings(
					new PortBinding(Ports.Binding.bindPort(hostPort), new ExposedPort(NATS_PORT)),
					new PortBinding(Ports.Binding.empty(), new ExposedPort(NATS_MONITORING_PORT))));
		authContainer.start();
	}

	private int getServerConnectionCount() throws Exception {

		var url = "http://127.0.0.1:" + authContainer.getMappedPort(NATS_MONITORING_PORT) + "/connz";

		try (var stream = java.net.URI.create(url).toURL().openStream()) {

			var body = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			var matcher = java.util.regex.Pattern.compile("\"num_connections\"\\s*:\\s*(\\d+)").matcher(body);

			if (!matcher.find()) {
				throw new IllegalStateException("num_connections not found in " + url + " response");
			}

			return Integer.parseInt(matcher.group(1));
		}
	}

	private void stopAuthNats() {
		authContainer.stop();
		authContainer = null;
	}

	private GenericObjectPool<Destination<?>> createPool(int hostPort) {

		var map = new HashMap<String, Object>();
		map.put("kind", "nats");
		map.put("servers", "nats://127.0.0.1:" + hostPort);
		map.put("subject", "test-subject");
		map.put("authentication-method", "username-and-password");
		map.put("username", USERNAME);
		map.put("password", GOOD_PASSWORD);

		var destinationConfig = DestinationUtils.createDestinationConfig(new MapConfiguration(map));
		destinationConfig.initialize();

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(destinationConfig);

		var poolConfig = new GenericObjectPoolConfig<Destination<?>>();
		poolConfig.setMaxTotal(2);
		poolConfig.setTestOnBorrow(true);

		return new GenericObjectPool<>(factory, poolConfig);
	}

	private AutoCloseable createAuthSubscriber(int hostPort, MessageCollector collector) throws Exception {

		var options = new Options.Builder()
			.server("nats://127.0.0.1:" + hostPort)
			.userInfo(USERNAME, GOOD_PASSWORD)
			.build();

		var connection = Nats.connect(options);
		var dispatcher = connection.createDispatcher(collector::onMessage);
		dispatcher.subscribe("test-subject");

		// the subscription is live once the server has processed the SUB (flush round-trips a PING)
		connection.flush(Duration.ofSeconds(5));

		return () -> {
			try {
				dispatcher.unsubscribe("test-subject");
				connection.close();
			} catch (Exception ignored) {
			}
		};
	}

	@Test
	public void shouldCullTerminallyClosedConnectionAndResumePublishing() throws Exception {

		// arrange: NATS with auth on a fixed host port, pool with test-on-borrow

		var hostPort = findFreePort();

		startAuthNats(hostPort, GOOD_PASSWORD);
		pool = createPool(hostPort);

		var collector = new MessageCollector();
		var subscriber = createAuthSubscriber(hostPort, collector);

		// healthy baseline: borrow, send, return

		var first = pool.borrowObject();
		first.send(createMessage("evt-001", "test-realm", false, "LOGIN", "application/json", "{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8), null, null));
		pool.returnObject(first);

		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> collector.getMessages().size() == 1);

		// act: rotate credentials on the same port; the client's reconnect attempts hit
		// repeated authorization violations and jnats closes the connection terminally

		ValidationUtils.tryClose(subscriber, "subscriber");
		stopAuthNats();
		startAuthNats(hostPort, ROTATED_PASSWORD);

		var firstNats = (NatsDestination) first;

		await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(1)).until(() ->
			firstNats.getConnection().getStatus() == Connection.Status.CLOSED);

		assertThat(first.isHealthy()).isFalse();

		// restore working credentials (fresh broker, same port - no KETE restart involved)

		stopAuthNats();
		startAuthNats(hostPort, GOOD_PASSWORD);

		var restoredCollector = new MessageCollector();
		try (var restoredSubscriber = createAuthSubscriber(hostPort, restoredCollector)) {

			// assert: test-on-borrow culls the dead instance, a fresh one is created,
			// and publishing resumes

			var second = pool.borrowObject();

			assertThat(second).isNotSameAs(first);
			assertThat(pool.getDestroyedByBorrowValidationCount()).isEqualTo(1);

			second.send(createMessage("evt-002", "test-realm", false, "LOGIN", "application/json", "{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8), null, null));
			pool.returnObject(second);

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> restoredCollector.getMessages().size() == 1);

			assertThat(restoredCollector.getMessages()).hasSize(1);

			// no connection leaks: the broker sees exactly the restored subscriber plus the
			// fresh destination's connection (the culled instance's connection is gone)

			await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500)).until(() -> getServerConnectionCount() == 2);
		}
	}
}
