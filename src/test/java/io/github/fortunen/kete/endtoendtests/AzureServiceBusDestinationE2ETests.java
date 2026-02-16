package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

class AzureServiceBusDestinationE2ETests extends EndToEndTestBase {

	private static final int AMQP_PORT = 5672;
	private static final String SA_PASSWORD = "Password123!";
	private static final String QUEUE_NAME = "keycloak-events";
	private static final String SQL_EDGE_IMAGE = "mcr.microsoft.com/azure-sql-edge:latest";
	private static final String SERVICEBUS_EMULATOR_IMAGE = "mcr.microsoft.com/azure-messaging/servicebus-emulator:latest";
	private static final String CONNECTION_STRING = "Endpoint=sb://servicebus-emulator;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";

	private static final String CONFIG_JSON = """
		{
		  "UserConfig": {
		    "Namespaces": [
		      {
		        "Name": "sbemulatorns",
		        "Queues": [
		          {
		            "Name": "keycloak-events",
		            "Properties": {
		              "DeadLetteringOnMessageExpiration": false,
		              "DefaultMessageTimeToLive": "PT1H",
		              "DuplicateDetectionHistoryTimeWindow": "PT20S",
		              "ForwardDeadLetteredMessagesTo": "",
		              "ForwardTo": "",
		              "LockDuration": "PT1M",
		              "MaxDeliveryCount": 10,
		              "RequiresDuplicateDetection": false,
		              "RequiresSession": false
		            }
		          }
		        ]
		      }
		    ],
		    "Logging": { "Type": "Console" }
		  }
		}
		""";

	private GenericContainer<?> sqlEdge;
	private GenericContainer<?> servicebusEmulator;
	private ServiceBusReceiverClient receiverClient;

	@AfterEach
	void tearDown() {
		if (receiverClient != null) {
			try { receiverClient.close(); } catch (Exception e) { /* ignore */ }
			receiverClient = null;
		}
		if (servicebusEmulator != null) {
			try { servicebusEmulator.stop(); } catch (Exception e) { /* ignore */ }
		}
		if (sqlEdge != null) {
			try { sqlEdge.stop(); } catch (Exception e) { /* ignore */ }
		}
		cleanupNetwork();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldForwardLoginEventToAzureServiceBus() throws Exception {

		// arrange — start SQL Edge (dependency) + Service Bus emulator

		sqlEdge = new GenericContainer<>(DockerImageName.parse(SQL_EDGE_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("sqledge")
			.withEnv("ACCEPT_EULA", "Y")
			.withEnv("MSSQL_SA_PASSWORD", SA_PASSWORD);
		sqlEdge.start();

		servicebusEmulator = new GenericContainer<>(DockerImageName.parse(SERVICEBUS_EMULATOR_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("servicebus-emulator")
			.withExposedPorts(AMQP_PORT)
			.withEnv("ACCEPT_EULA", "Y")
			.withEnv("SQL_SERVER", "sqledge")
			.withEnv("MSSQL_SA_PASSWORD", SA_PASSWORD)
			.withCopyToContainer(Transferable.of(CONFIG_JSON, 0777), "/ServiceBus_Emulator/ConfigFiles/Config.json");
		servicebusEmulator.start();

		waitForPortReady("127.0.0.1", servicebusEmulator.getMappedPort(AMQP_PORT));

		// build verification receiver pointing at emulator's mapped port

		var verificationConnectionString = "Endpoint=sb://127.0.0.1:" + servicebusEmulator.getMappedPort(AMQP_PORT) + ";SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
		
		receiverClient = new ServiceBusClientBuilder()
			.connectionString(verificationConnectionString)
			.receiver()
			.queueName(QUEUE_NAME)
			.buildClient();

		// wait for emulator to be fully ready (SDK-level probe)

		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				receiverClient.peekMessage();
				return true;
			} catch (Exception e) {
				return false;
			}
		});

		// configure Keycloak — destination.kind=azure-servicebus pointing to emulator on Docker network

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.sb-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.sb-test.destination.kind", "azure-servicebus");
		envVars.put("kete.routes.sb-test.destination.connection-string", CONNECTION_STRING);
		envVars.put("kete.routes.sb-test.destination.queue", QUEUE_NAME);
		envVars.put("kete.routes.sb-test.destination.custom-endpoint-address", "http://servicebus-emulator:" + AMQP_PORT);
		envVars.put("kete.routes.sb-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {

			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — poll Service Bus queue for messages

				await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
					var messages = receiverClient.receiveMessages(10, Duration.ofSeconds(5));
					var bodies = messages.stream().map(m -> m.getBody().toString()).toList();
					assertThat(bodies).isNotEmpty();
					assertThat(bodies).anyMatch(body -> body.contains(TEST_REALM));
				});

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}
}
