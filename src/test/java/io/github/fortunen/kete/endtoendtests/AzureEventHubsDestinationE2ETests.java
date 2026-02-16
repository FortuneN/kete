package io.github.fortunen.kete.endtoendtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;

class AzureEventHubsDestinationE2ETests extends EndToEndTestBase {

	private static final int AMQP_PORT = 5672;
	private static final String CONSUMER_GROUP = "$Default";
	private static final String EVENT_HUB_NAME = "keycloak-events";
	private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";
	private static final String EVENTHUBS_EMULATOR_IMAGE = "mcr.microsoft.com/azure-messaging/eventhubs-emulator:latest";
	private static final String CONNECTION_STRING = "Endpoint=sb://eventhubs-emulator;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;EntityPath=keycloak-events";

	private static final String CONFIG_JSON = """
		{
		  "UserConfig": {
		    "NamespaceConfig": [
		      {
		        "Type": "EventHub",
		        "Name": "emulatorns1",
		        "Entities": [
		          {
		            "Name": "keycloak-events",
		            "PartitionCount": "2",
		            "ConsumerGroups": []
		          }
		        ]
		      }
		    ],
		    "LoggingConfig": { "Type": "Console" }
		  }
		}
		""";

	private GenericContainer<?> azurite;
	private GenericContainer<?> eventhubsEmulator;
	private EventHubConsumerClient consumerClient;

	@AfterEach
	void tearDown() {
		if (consumerClient != null) {
			try { consumerClient.close(); } catch (Exception e) { /* ignore */ }
			consumerClient = null;
		}
		if (eventhubsEmulator != null) {
			try { eventhubsEmulator.stop(); } catch (Exception e) { /* ignore */ }
		}
		if (azurite != null) {
			try { azurite.stop(); } catch (Exception e) { /* ignore */ }
		}
		cleanupNetwork();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldForwardLoginEventToAzureEventHubs() throws Exception {

		// arrange — start Azurite (dependency) + Event Hubs emulator

		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("azurite")
			.withExposedPorts(10000, 10001, 10002);
		azurite.start();

		waitForPortReady("127.0.0.1", azurite.getMappedPort(10000));

		eventhubsEmulator = new GenericContainer<>(DockerImageName.parse(EVENTHUBS_EMULATOR_IMAGE))
			.withNetwork(createNetwork())
			.withNetworkAliases("eventhubs-emulator")
			.withExposedPorts(AMQP_PORT)
			.withEnv("ACCEPT_EULA", "Y")
			.withEnv("BLOB_SERVER", "azurite")
			.withEnv("METADATA_SERVER", "azurite")
			.withCopyToContainer(Transferable.of(CONFIG_JSON, 0777), "/Eventhubs_Emulator/ConfigFiles/Config.json");
		eventhubsEmulator.start();

		waitForPortReady("127.0.0.1", eventhubsEmulator.getMappedPort(AMQP_PORT));

		// build verification consumer pointing at emulator's mapped port

		var verificationConnectionString = "Endpoint=sb://" + "127.0.0.1" + ":" + eventhubsEmulator.getMappedPort(AMQP_PORT) + ";SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
		
		consumerClient = new EventHubClientBuilder()
			.connectionString(verificationConnectionString, EVENT_HUB_NAME)
			.consumerGroup(CONSUMER_GROUP)
			.buildConsumerClient();

		// wait for emulator to be fully ready (SDK-level probe)

		await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				consumerClient.getPartitionIds().stream().toList();
				return true;
			} catch (Exception e) {
				return false;
			}
		});

		// configure Keycloak — destination.kind=azure-eventhubs pointing to emulator on Docker network

		var envVars = new HashMap<String, String>();
		envVars.put("kete.routes.eh-test.realm-matchers.filter", "list:" + TEST_REALM);
		envVars.put("kete.routes.eh-test.destination.kind", "azure-eventhubs");
		envVars.put("kete.routes.eh-test.destination.connection-string", CONNECTION_STRING);
		envVars.put("kete.routes.eh-test.destination.event-hub", EVENT_HUB_NAME);
		envVars.put("kete.routes.eh-test.serializer.kind", "json");

		try (var keycloak = createKeycloakContainer(envVars)) {
			
			keycloak.start();

			try (var adminClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(), keycloak.getAdminPassword(), "admin-cli")) {
				
				createTestRealm(adminClient);

				// act

				triggerLoginEvent(keycloak);

				// assert — poll Event Hub for events

				await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
					
					var allBodies = new ArrayList<String>();
					var partitionIds = consumerClient.getPartitionIds().stream().toList();
					
					for (var partitionId : partitionIds) {
						
						var events = consumerClient.receiveFromPartition(partitionId, 10, EventPosition.earliest(), Duration.ofSeconds(5));
						
						for (var partitionEvent : events) {
							allBodies.add(partitionEvent.getData().getBodyAsString());
						}
					}

					assertThat(allBodies).isNotEmpty();
					assertThat(allBodies).anyMatch(body -> body.contains(TEST_REALM));
				});

				// cleanup

				cleanupTestRealm(adminClient);
			}
		}
	}
}
