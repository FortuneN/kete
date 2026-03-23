package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

class MetricsE2ETests extends EndToEndTestBase {

	private static final String KAFKA_TOPIC = "keycloak-events";
	private KafkaContainer kafka;

	@AfterEach
	void tearDown() {
		if (kafka != null) {
			kafka.stop();
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldExposeMetricsWhenEnabled() throws Exception {

		// arrange - Kafka destination required to generate forwarding metrics

		kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0")).withNetwork(createNetwork()).withNetworkAliases("kafka").withListener("kafka:19092");
		kafka.start();
		waitForKafkaReady(kafka);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.metrics.enabled", "true");
		envVars.put("kete.routes.metrics-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.metrics-test.destination.kind", "kafka");
		envVars.put("kete.routes.metrics-test.destination.bootstrap.servers", "kafka:19092");
		envVars.put("kete.routes.metrics-test.destination.topic", KAFKA_TOPIC);
		envVars.put("kete.routes.metrics-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainerWithMetrics(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(
				keycloak.getAuthServerUrl(),
				"master",
				keycloak.getAdminUsername(),
				keycloak.getAdminPassword(),
				"admin-cli"
			)) {
				createTestRealm(adminClient);

				// Consume Kafka to ensure forwarding happens
				var consumerProps = new Properties();
				consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
				consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "metrics-test-group");
				consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
				consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

				try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
					consumer.subscribe(List.of(KAFKA_TOPIC));

					// act

					triggerLoginEvent(keycloak);

					// Wait for message to be forwarded to Kafka
					await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
						var records = consumer.poll(Duration.ofSeconds(1));
						return !records.isEmpty();
					});
				}

				// Wait for metrics to be recorded (Keycloak 25+ serves metrics on management port)
				var metricsUrl = keycloak.getMgmtServerUrl() + "/metrics";
				var httpClient = HttpClient.newHttpClient();
				var request = HttpRequest.newBuilder()
					.uri(URI.create(metricsUrl))
					.GET()
					.build();

				var metricsBody = new String[1];
				await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
					var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
					metricsBody[0] = response.body();
					return metricsBody[0].contains("kete_events_forwarded_total");
				});

				// assert

				assertThat(metricsBody[0])
					.as("Metrics should contain kete.events.forwarded.total")
					.contains("kete_events_forwarded_total");

				assertThat(metricsBody[0])
					.as("Metrics should contain kete.forward.duration.seconds")
					.contains("kete_forward_duration_seconds");

				assertThat(metricsBody[0])
					.as("Metrics should contain kete.routes.active")
					.contains("kete_routes_active");

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}

	@Test
	void shouldNotExposeMetricsWhenDisabled() throws Exception {

		// arrange - No destination needed, just checking metrics endpoint

		var envVars = new HashMap<String, String>();
		envVars.put("kete.metrics.enabled", "false");

		try (var keycloak = createKeycloakContainerWithMetrics(envVars)) {
			keycloak.start();

			// act - fetch metrics endpoint (Keycloak 25+ serves metrics on management port)

			var metricsUrl = keycloak.getMgmtServerUrl() + "/metrics";
			var httpClient = HttpClient.newHttpClient();
			var request = HttpRequest.newBuilder()
				.uri(URI.create(metricsUrl))
				.GET()
				.build();

			var metricsBody = new String[1];
			await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
				var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				metricsBody[0] = response.body();
				return response.statusCode() == 200;
			});

			// assert - kete metrics should NOT be present when disabled

			assertThat(metricsBody[0])
				.as("Metrics should NOT contain kete metrics when disabled")
				.doesNotContain("kete_events_forwarded_total")
				.doesNotContain("kete_events_failed_total")
				.doesNotContain("kete_forward_duration_seconds")
				.doesNotContain("kete_routes_active");
		}
	}
}
