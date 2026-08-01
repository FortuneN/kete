package io.github.fortunen.kete.integrationtests.mqtt3destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationPooledObjectFactory;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.DestinationUtils;

/**
 * Broker-outage resilience: while the broker is down the destination must report
 * unhealthy (so the pool stops serving it), and once the broker returns publishing
 * must resume without restarting anything - either via the client's own reconnect
 * or via the pool culling the dead instance and creating a fresh one.
 */
public class isHealthyTests {

	private static final int BROKER_PORT = 1883;

	private GenericContainer<?> broker;
	private GenericObjectPool<Destination<?>> pool;

	@AfterEach
	void cleanUp() {

		if (pool != null) {
			try { pool.close(); } catch (Exception exception) { /* ignore */ }
			pool = null;
		}

		if (broker != null) {
			try { broker.stop(); } catch (Exception exception) { /* ignore */ }
			broker = null;
		}
	}

	private static int findFreePort() throws Exception {
		try (var socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	@SuppressWarnings("resource")
	private void startBroker(int hostPort) {

		broker = new GenericContainer<>(DockerImageName.parse("hivemq/hivemq-ce:2024.3"))
			.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig()
				.withPortBindings(new PortBinding(Ports.Binding.bindPort(hostPort), new ExposedPort(BROKER_PORT))));
		broker.start();
	}

	private GenericObjectPool<Destination<?>> createPool(int hostPort) {

		var map = new HashMap<String, Object>();
		map.put("kind", "mqtt-3");
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(hostPort));
		map.put("topic", "test-topic");

		var destinationConfig = DestinationUtils.createDestinationConfig(new MapConfiguration(map));
		destinationConfig.initialize();

		var factory = new DestinationPooledObjectFactory();
		factory.setDestinationConfig(destinationConfig);

		var poolConfig = new GenericObjectPoolConfig<Destination<?>>();
		poolConfig.setMaxTotal(2);
		poolConfig.setTestOnBorrow(true);

		return new GenericObjectPool<>(factory, poolConfig);
	}

	private static EventMessage createLoginMessage(String eventId) {
		return new EventMessage("test-realm", eventId, "{\"type\":\"LOGIN\"}".getBytes(StandardCharsets.UTF_8), "LOGIN", "application/json", null, Constants.EVENT, null, null);
	}

	private Destination<?> awaitSuccessfulSend(String eventIdPrefix, Duration timeout) {

		var attempt = new AtomicInteger();
		var lastUsed = new AtomicReference<Destination<?>>();

		await().atMost(timeout).pollInterval(Duration.ofSeconds(2)).until(() -> {
			Destination<?> destination = null;
			try {
				destination = pool.borrowObject();
				destination.send(createLoginMessage(eventIdPrefix + "-" + attempt.incrementAndGet()));
				pool.returnObject(destination);
				lastUsed.set(destination);
				return true;
			} catch (Exception exception) {
				if (destination != null) {
					try { pool.invalidateObject(destination); } catch (Exception ignored) { }
				}
				return false;
			}
		});

		return lastUsed.get();
	}

	@Test
	public void shouldReportUnhealthyDuringBrokerOutageAndResumeAfterRestart() throws Exception {

		// arrange: broker on a fixed host port so clients can reconnect after restart

		var hostPort = findFreePort();

		startBroker(hostPort);
		pool = createPool(hostPort);

		var first = awaitSuccessfulSend("evt-baseline", Duration.ofMinutes(3));

		assertThat(first.isHealthy()).isTrue();

		// act: kill the broker; the idle instance must report unhealthy

		broker.stop();
		broker = null;

		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1)).until(() -> !first.isHealthy());

		// restore the broker on the same port; publishing must resume (client reconnect
		// or pool cull-and-recreate - either recovery path satisfies the invariant)

		startBroker(hostPort);

		awaitSuccessfulSend("evt-resumed", Duration.ofMinutes(3));
	}
}
