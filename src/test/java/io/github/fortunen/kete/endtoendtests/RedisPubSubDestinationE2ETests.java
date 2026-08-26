package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;

import java.util.List;

class RedisPubSubDestinationE2ETests extends EndToEndTestBase {

	private static final String REDIS_CHANNEL_TEMPLATE = "keycloak-events-${realmLowerCase}";
	private static final String REDIS_CHANNEL_RESOLVED = "keycloak-events-" + TEST_REALM.toLowerCase();
	private GenericContainer<?> redis;

	@AfterEach
	void tearDown() {
		if (redis != null) {
			redis.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToRedisPubSub() throws Exception {

		// arrange

		redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withNetwork(createNetwork())
			.withNetworkAliases("redis")
			.withExposedPorts(6379);
		redis.start();
		waitForRedisReady();

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.redis-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.redis-test.destination.kind", "redis-pubsub");
		envVars.put("kete.routes.redis-test.destination.host", "redis");
		envVars.put("kete.routes.redis-test.destination.port", "6379");
		envVars.put("kete.routes.redis-test.destination.channel", REDIS_CHANNEL_TEMPLATE);
		envVars.put("kete.routes.redis-test.serializer.kind", "yaml");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// subscribe before triggering event
				var collector = new MessageCollector();
				var subscriber = createSubscriber(REDIS_CHANNEL_RESOLVED, collector);

				// act

				triggerLoginEvent(keycloak);

				// assert

				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> !collector.getMessages().isEmpty());

				assertThat(collector.getMessages()).hasSizeGreaterThan(0);
				var message = collector.getMessages().get(0);
				// YAML serializer assertions
				assertThat(message).satisfiesAnyOf(
					b -> assertThat(b).contains("type:"),
					b -> assertThat(b).contains("operationType:")
				);
				assertThat(message).satisfiesAnyOf(
					b -> assertThat(b).contains("realmName:"),
					b -> assertThat(b).contains("realmId:")
				);
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
				var uri = RedisURI.builder()
					.withHost("127.0.0.1")
					.withPort(redis.getMappedPort(6379))
					.build();
				var client = RedisClient.create(uri);
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

	private AutoCloseable createSubscriber(String channel, MessageCollector collector) {
		var uri = RedisURI.builder()
			.withHost("127.0.0.1")
			.withPort(redis.getMappedPort(6379))
			.build();

		var client = RedisClient.create(uri);
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
