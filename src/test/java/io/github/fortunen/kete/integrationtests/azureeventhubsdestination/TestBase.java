package io.github.fortunen.kete.integrationtests.azureeventhubsdestination;

import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.azureeventhubs.AzureEventHubsDestination;
import io.github.fortunen.kete.destinations.azureeventhubs.AzureEventHubsDestinationConfig;

public class TestBase {

	private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";
	private static final String EVENTHUBS_EMULATOR_IMAGE = "mcr.microsoft.com/azure-messaging/eventhubs-emulator:latest";
	private static final int AMQP_PORT = 5672;
	protected static final String EVENT_HUB_NAME = "keycloak-events";
	protected static final String CONSUMER_GROUP = "$Default";

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

	protected Network network;
	protected GenericContainer<?> azurite;
	protected GenericContainer<?> emulator;
	protected AzureEventHubsDestination destination;
	protected AzureEventHubsDestinationConfig config;
	protected EventHubConsumerClient consumerClient;

	@BeforeEach
	void setUp() {
		destination = new AzureEventHubsDestination();
		config = new AzureEventHubsDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (consumerClient != null) {
			try { consumerClient.close(); } catch (Exception e) { /* ignore */ }
			consumerClient = null;
		}
		if (emulator != null) {
			try { emulator.stop(); } catch (Exception e) { /* ignore */ }
			emulator = null;
		}
		if (azurite != null) {
			try { azurite.stop(); } catch (Exception e) { /* ignore */ }
			azurite = null;
		}
		if (network != null) {
			try { network.close(); } catch (Exception e) { /* ignore */ }
			network = null;
		}
	}

	@SuppressWarnings("resource")
	protected void startEmulator() {
		network = Network.newNetwork();

		azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withNetwork(network)
			.withNetworkAliases("azurite")
			.withExposedPorts(10000, 10001, 10002);
		azurite.start();

		waitForAzuriteReady();

		emulator = new GenericContainer<>(DockerImageName.parse(EVENTHUBS_EMULATOR_IMAGE))
			.withNetwork(network)
			.withNetworkAliases("eventhubs-emulator")
			.withExposedPorts(AMQP_PORT)
			.withEnv("ACCEPT_EULA", "Y")
			.withEnv("BLOB_SERVER", "azurite")
			.withEnv("METADATA_SERVER", "azurite")
			.withCopyToContainer(Transferable.of(CONFIG_JSON, 0777), "/Eventhubs_Emulator/ConfigFiles/Config.json");
		emulator.start();

		waitForEmulatorReady();

		consumerClient = new EventHubClientBuilder()
			.connectionString(buildConnectionString(), EVENT_HUB_NAME)
			.consumerGroup(CONSUMER_GROUP)
			.buildConsumerClient();
	}

	protected void configureDestination() {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventhubs");
		map.put("connection-string", buildConnectionString());
		map.put("event-hub", EVENT_HUB_NAME);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected String receiveMessage() {
		var partitionIds = consumerClient.getPartitionIds().stream().toList();
		var allBodies = new ArrayList<String>();

		for (var partitionId : partitionIds) {
			var events = consumerClient.receiveFromPartition(partitionId, 10, EventPosition.earliest(), Duration.ofSeconds(5));
			for (var partitionEvent : events) {
				allBodies.add(partitionEvent.getData().getBodyAsString());
			}
		}

		return allBodies.isEmpty() ? null : allBodies.get(0);
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}

	private String buildConnectionString() {
		return "Endpoint=sb://127.0.0.1:" + emulator.getMappedPort(AMQP_PORT)
			+ ";SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
	}

	private void waitForAzuriteReady() {
		var url = "http://127.0.0.1:" + azurite.getMappedPort(10000) + "/";
		var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
				client.send(request, HttpResponse.BodyHandlers.discarding());
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}

	private void waitForEmulatorReady() {
		await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(2)).until(() -> {
			try {
				var tempClient = new EventHubClientBuilder()
					.connectionString(buildConnectionString(), EVENT_HUB_NAME)
					.consumerGroup(CONSUMER_GROUP)
					.buildConsumerClient();
				tempClient.getPartitionIds();
				tempClient.close();
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}
}
