package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.lettuce.core.Range;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StreamMessage;

import java.util.List;

class RedisStreamDestinationE2ETests extends EndToEndTestBase {

	private static final String REDIS_STREAM_TEMPLATE = "keycloak-events-${realmLowerCase}";
	private static final String REDIS_STREAM_RESOLVED = "keycloak-events-" + TEST_REALM.toLowerCase();
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
	void shouldForwardLoginEventToRedisStream() throws Exception {

		// arrange

		redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withNetwork(createNetwork())
			.withNetworkAliases("redis")
			.withExposedPorts(6379);
		redis.start();
		waitForRedisReady();

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.redis-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.redis-test.destination.kind", "redis-stream");
		envVars.put("kete.routes.redis-test.destination.host", "redis");
		envVars.put("kete.routes.redis-test.destination.port", "6379");
		envVars.put("kete.routes.redis-test.destination.stream", REDIS_STREAM_TEMPLATE);
		envVars.put("kete.routes.redis-test.serializer.kind", "yaml");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert

				await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> !readFromStream(REDIS_STREAM_RESOLVED).isEmpty());

				var messages = readFromStream(REDIS_STREAM_RESOLVED);
				assertThat(messages).hasSizeGreaterThan(0);

				var message = messages.get(0);
				var body = message.getBody().get("body");

				// YAML serializer assertions
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("type:"),
					b -> assertThat(b).contains("operationType:")
				);
				assertThat(body).satisfiesAnyOf(
					b -> assertThat(b).contains("realmName:"),
					b -> assertThat(b).contains("realmId:")
				);
				assertThat(body).contains(TEST_REALM);

				// Check headers are present
				assertThat(message.getBody()).containsKey("eventtype");
				assertThat(message.getBody()).containsKey("contenttype");

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}

	private void waitForRedisReady() {
		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
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

	private List<StreamMessage<String, String>> readFromStream(String stream) {
		var uri = RedisURI.builder()
			.withHost("127.0.0.1")
			.withPort(redis.getMappedPort(6379))
			.build();

		var client = RedisClient.create(uri);

		try (var connection = client.connect()) {
			return connection.sync().xrange(stream, Range.unbounded());
		} finally {
			client.close();
		}
	}
}
