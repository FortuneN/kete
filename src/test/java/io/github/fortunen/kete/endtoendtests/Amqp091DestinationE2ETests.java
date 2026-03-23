package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

class Amqp091DestinationE2ETests extends EndToEndTestBase {

	private RabbitMQContainer rabbitmq;
	private static final String QUEUE_NAME = "keycloak-events";
	private static final String EXCHANGE_NAME = "keycloak-exchange";

	@AfterEach
	void tearDown() {
		
		if (rabbitmq != null) {
			rabbitmq.stop();
		}

		cleanupNetwork();
	}
	
	@Test
	@SuppressWarnings("resource")
	void shouldForwardLoginEventToRabbitMQQueue() throws Exception {

		// arrange

		rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management")).withNetwork(createNetwork()).withNetworkAliases("rabbitmq");
		rabbitmq.start();

		var factory = new ConnectionFactory();
		
		factory.setHost("127.0.0.1");
		factory.setPort(rabbitmq.getAmqpPort());
		factory.setUsername(rabbitmq.getAdminUsername());
		factory.setPassword(rabbitmq.getAdminPassword());

		waitForRabbitMqReady(factory);

		try (Connection connection = factory.newConnection();

			var channel = connection.createChannel()) {
			
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
			channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
		}

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.amqp-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.amqp-test.destination.kind", "amqp-0.9.1");
		envVars.put("kete.routes.amqp-test.destination.host", "rabbitmq");
		envVars.put("kete.routes.amqp-test.destination.port", "5672");
		envVars.put("kete.routes.amqp-test.destination.username", rabbitmq.getAdminUsername());
		envVars.put("kete.routes.amqp-test.destination.password", rabbitmq.getAdminPassword());
		envVars.put("kete.routes.amqp-test.destination.exchange", EXCHANGE_NAME);
		envVars.put("kete.routes.amqp-test.serializer.kind", "xml");

		try (var keycloak = createKeycloakContainer(envVars)) {
			
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert

				try (var connection = factory.newConnection(); var channel = connection.createChannel()) {

					var response = new GetResponse[1];

					await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
						response[0] = channel.basicGet(QUEUE_NAME, true);
						return response[0] != null;
					});

					assertThat(response[0]).isNotNull();

					var body = new String(response[0].getBody());
					
					// XML serializer assertions - check for XML format
					
					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("<type>"),
						b -> assertThat(b).contains("<operationType>")
					);

					assertThat(body).satisfiesAnyOf(
						b -> assertThat(b).contains("<realmName>"),
						b -> assertThat(b).contains("<realmId>")
					);

					assertThat(body).contains(TEST_REALM);
				}

				// cleanup
				
				cleanupTestRealm(adminClient);
			}
		}
	}
}
