package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;

class RedisSentinelDestinationE2ETests extends EndToEndTestBase {

	private static final String REDIS_CHANNEL = "keycloak-events-sentinel";
	private static final String MASTER_NAME = "mymaster";
	private static final int REDIS_PORT = 6379;
	private static final int SENTINEL_PORT = 26379;

	// announce-hostnames makes the sentinel hand out "redis" (the network alias) as the master address

	private static final String SENTINEL_CONFIG = String.join("\n",
		"port " + SENTINEL_PORT,
		"sentinel resolve-hostnames yes",
		"sentinel announce-hostnames yes",
		"sentinel monitor " + MASTER_NAME + " redis " + REDIS_PORT + " 1",
		"sentinel down-after-milliseconds " + MASTER_NAME + " 5000",
		"sentinel failover-timeout " + MASTER_NAME + " 10000",
		"");

	private GenericContainer<?> redis;
	private GenericContainer<?> sentinel;

	@AfterEach
	void tearDown() {
		if (sentinel != null) {
			sentinel.stop();
		}
		if (redis != null) {
			redis.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToTheMasterDiscoveredThroughSentinel() throws Exception {

		// arrange

		var network = createNetwork();

		redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withNetwork(network)
			.withNetworkAliases("redis")
			.withExposedPorts(REDIS_PORT);
		redis.start();
		waitForRedisReady();

		sentinel = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withNetwork(network)
			.withNetworkAliases("sentinel")
			.withExposedPorts(SENTINEL_PORT)
			.withCopyToContainer(Transferable.of(SENTINEL_CONFIG.getBytes(StandardCharsets.UTF_8), 0666), "/tmp/sentinel.conf")
			.withCommand("redis-sentinel", "/tmp/sentinel.conf");
		sentinel.start();
		waitForSentinelReady();

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.redis-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.redis-test.destination.kind", "redis-pubsub");
		envVars.put("kete.routes.redis-test.destination.mode", "sentinel");
		envVars.put("kete.routes.redis-test.destination.sentinel-nodes", "sentinel:" + SENTINEL_PORT);
		envVars.put("kete.routes.redis-test.destination.sentinel-master-id", MASTER_NAME);
		envVars.put("kete.routes.redis-test.destination.channel", REDIS_CHANNEL);
		envVars.put("kete.routes.redis-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// subscribe on the master directly before triggering the event
				var collector = new MessageCollector();
				var subscriber = createSubscriber(collector);

				// act

				triggerLoginEvent(keycloak);

				// assert

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> !collector.getMessages().isEmpty());

				var message = collector.getMessages().get(0);
				assertThat(message).contains("LOGIN");
				assertThat(message).contains(TEST_REALM);

				subscriber.close();

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}

	private void waitForRedisReady() {
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var client = RedisClient.create(RedisURI.create("127.0.0.1", redis.getMappedPort(REDIS_PORT)));
				try (var connection = client.connect()) {
					return "PONG".equalsIgnoreCase(connection.sync().ping());
				} finally {
					client.close();
				}
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void waitForSentinelReady() {
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var client = RedisClient.create();
				try (var connection = client.connectSentinel(RedisURI.create("127.0.0.1", sentinel.getMappedPort(SENTINEL_PORT)))) {
					var master = connection.sync().master(MASTER_NAME);
					return master != null && "redis".equals(master.get("ip")) && master.getOrDefault("flags", "").contains("master");
				} finally {
					client.close();
				}
			} catch (Exception e) {
				return false;
			}
		});
	}

	private static class MessageCollector extends RedisPubSubAdapter<String, String> {
		private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

		@Override
		public void message(String channel, String message) {
			messages.add(message);
		}

		public List<String> getMessages() {
			return messages;
		}
	}

	private AutoCloseable createSubscriber(MessageCollector collector) {
		var client = RedisClient.create(RedisURI.create("127.0.0.1", redis.getMappedPort(REDIS_PORT)));
		var connection = client.connectPubSub();

		connection.addListener(collector);
		connection.sync().subscribe(REDIS_CHANNEL);

		return () -> {
			try {
				connection.close();
				client.close();
			} catch (Exception ignored) {
			}
		};
	}
}
