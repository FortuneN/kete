package io.github.fortunen.kete.integrationtests.mqtt5destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

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
 *
 * The outage is induced with docker pause/unpause: the container and its mapped
 * host port stay alive while all processing freezes, so clients detect a dead
 * connection and can later reconnect to the same address.
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

	@SuppressWarnings("resource")
	private void startBroker() {

		broker = new GenericContainer<>(DockerImageName.parse("hivemq/hivemq-ce:2024.3"))
			.withEnv("JAVA_OPTS", "-Xms256m -Xmx1g")
			.withLogConsumer(frame -> System.out.print("[HIVEMQ] " + frame.getUtf8String()))
			.withExposedPorts(BROKER_PORT)
			.waitingFor(Wait.forLogMessage(".*Started HiveMQ in.*", 1).withStartupTimeout(Duration.ofMinutes(5)));
		broker.start();
	}

	private void pauseBrokerProcess() {
		broker.getDockerClient().pauseContainerCmd(broker.getContainerId()).exec();
	}

	private void resumeBrokerProcess() {
		broker.getDockerClient().unpauseContainerCmd(broker.getContainerId()).exec();
	}

	private GenericObjectPool<Destination<?>> createPool(int mappedPort) {

		var map = new HashMap<String, Object>();
		map.put("kind", "mqtt-5");
		map.put("host", "127.0.0.1");
		map.put("port", String.valueOf(mappedPort));
		map.put("topic", "test-topic");
		// a short keep-alive lets Paho notice the stopped broker in seconds instead of two minutes
		map.put("keep-alive-interval-seconds", "5");

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
		var lastError = new AtomicReference<Exception>();

		try {
			await().atMost(timeout).pollInterval(Duration.ofSeconds(2)).until(() -> {
			Destination<?> destination = null;
			try {
				destination = pool.borrowObject();
				destination.send(createLoginMessage(eventIdPrefix + "-" + attempt.incrementAndGet()));
				pool.returnObject(destination);
				lastUsed.set(destination);
				return true;
			} catch (Exception exception) {
				lastError.set(exception);
				if (destination != null) {
					try { pool.invalidateObject(destination); } catch (Exception ignored) { }
				}
				return false;
			}
			});
		} catch (org.awaitility.core.ConditionTimeoutException timeoutException) {
			throw new AssertionError("no successful send within " + timeout + "; last error: " + lastError.get(), lastError.get());
		}

		return lastUsed.get();
	}

	@Test
	public void shouldReportUnhealthyDuringBrokerOutageAndResumeAfterRestart() throws Exception {

		// arrange

		startBroker();

		var mappedPort = broker.getMappedPort(BROKER_PORT);

		pool = createPool(mappedPort);

		var first = awaitSuccessfulSend("evt-baseline", Duration.ofMinutes(3));

		assertThat(first.isHealthy()).isTrue();

		// act: freeze the broker (docker pause keeps the container and its port mapping
		// alive but stops all processing); the client's keepalive declares the connection
		// dead and the idle instance must report unhealthy

		pauseBrokerProcess();

		await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(1)).until(() -> !first.isHealthy());

		// unfreeze; same container, same mapped port - publishing must resume via
		// client reconnect or pool cull-and-recreate, either path counts

		resumeBrokerProcess();

		awaitSuccessfulSend("evt-resumed", Duration.ofMinutes(3));
	}
}
