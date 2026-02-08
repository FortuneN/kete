package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

class KafkaDestinationE2ETests extends EndToEndTestBase {

	private static final String KAFKA_TOPIC_TEMPLATE = "keycloak-events-${realmLowerCase}";
	private static final String KAFKA_TOPIC_RESOLVED = "keycloak-events-" + TEST_REALM.toLowerCase();
	private KafkaContainer kafka;

	@AfterEach
	void tearDown() {
		if (kafka != null) {
			kafka.stop();
		}
		cleanupNetwork();
	}

	@Test
	void shouldForwardLoginEventToKafkaTopic() throws Exception {

		// arrange

		kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0")).withNetwork(createNetwork()).withNetworkAliases("kafka").withListener("kafka:19092");
		kafka.start();
		waitForKafkaReady(kafka);

		var envVars = new HashMap<String, String>();
		envVars.put("kete.enabled", "true");
		envVars.put("kete.routes.kafka-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.kafka-test.destination.kind", "kafka");
		envVars.put("kete.routes.kafka-test.destination.bootstrap.servers", "kafka:19092");
		envVars.put("kete.routes.kafka-test.destination.topic", KAFKA_TOPIC_TEMPLATE);
		envVars.put("kete.routes.kafka-test.serializer.kind", "yaml");

		try (var keycloak = createKeycloakContainer(envVars)) {
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert

				var consumerProps = new Properties();
				consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
				consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
				consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
				consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

				try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
					consumer.subscribe(List.of(KAFKA_TOPIC_RESOLVED));

					ConsumerRecords<String, String> records = ConsumerRecords.empty();
					for (int i = 0; i < 30 && records.isEmpty(); i++) {
						records = consumer.poll(Duration.ofSeconds(1));
					}

					assertThat(records.count()).isGreaterThan(0);

					var record = records.iterator().next();
					// YAML serializer assertions - check for YAML format markers
					assertThat(record.value()).satisfiesAnyOf(
						b -> assertThat(b).contains("type:"),
						b -> assertThat(b).contains("operationType:")
					);
					assertThat(record.value()).satisfiesAnyOf(
						b -> assertThat(b).contains("realmName:"),
						b -> assertThat(b).contains("realmId:")
					);
					assertThat(record.value()).contains(TEST_REALM);
				}

				// cleanup
				cleanupTestRealm(adminClient);
			}
		}
	}
}
