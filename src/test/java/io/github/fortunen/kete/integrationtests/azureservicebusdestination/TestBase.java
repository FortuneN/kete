package io.github.fortunen.kete.integrationtests.azureservicebusdestination;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.azureservicebus.AzureServiceBusDestination;
import io.github.fortunen.kete.destinations.azureservicebus.AzureServiceBusDestinationConfig;

public class TestBase {

	private static final String SQL_EDGE_IMAGE = "mcr.microsoft.com/azure-sql-edge:latest";
	private static final String SERVICEBUS_EMULATOR_IMAGE = "mcr.microsoft.com/azure-messaging/servicebus-emulator:latest";
	private static final int AMQP_PORT = 5672;
	private static final String SA_PASSWORD = "Password123!";
	protected static final String QUEUE_NAME = "keycloak-events";

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

	protected Network network;
	protected GenericContainer<?> sqlEdge;
	protected GenericContainer<?> emulator;
	protected AzureServiceBusDestination destination;
	protected AzureServiceBusDestinationConfig config;
	protected ServiceBusReceiverClient receiverClient;

	@BeforeEach
	void setUp() {
		destination = new AzureServiceBusDestination();
		config = new AzureServiceBusDestinationConfig();
	}

	@AfterEach
	void tearDown() {
		if (destination != null) {
			try { destination.close(); } catch (Exception e) { /* ignore */ }
		}
		if (receiverClient != null) {
			try { receiverClient.close(); } catch (Exception e) { /* ignore */ }
			receiverClient = null;
		}
		if (emulator != null) {
			try { emulator.stop(); } catch (Exception e) { /* ignore */ }
			emulator = null;
		}
		if (sqlEdge != null) {
			try { sqlEdge.stop(); } catch (Exception e) { /* ignore */ }
			sqlEdge = null;
		}
		if (network != null) {
			try { network.close(); } catch (Exception e) { /* ignore */ }
			network = null;
		}
	}

	@SuppressWarnings("resource")
	protected void startEmulator() {
		network = Network.newNetwork();

		sqlEdge = new GenericContainer<>(DockerImageName.parse(SQL_EDGE_IMAGE))
			.withNetwork(network)
			.withNetworkAliases("sqledge")
			.withEnv("ACCEPT_EULA", "Y")
			.withEnv("MSSQL_SA_PASSWORD", SA_PASSWORD);
		sqlEdge.start();

		emulator = new GenericContainer<>(DockerImageName.parse(SERVICEBUS_EMULATOR_IMAGE))
			.withNetwork(network)
			.withNetworkAliases("servicebus-emulator")
			.withExposedPorts(AMQP_PORT)
			.withEnv("ACCEPT_EULA", "Y")
			.withEnv("SQL_SERVER", "sqledge")
			.withEnv("MSSQL_SA_PASSWORD", SA_PASSWORD)
			.withCopyToContainer(Transferable.of(CONFIG_JSON, 0777), "/ServiceBus_Emulator/ConfigFiles/Config.json");
		emulator.start();

		waitForEmulatorReady();

		receiverClient = new ServiceBusClientBuilder()
			.connectionString(buildConnectionString())
			.receiver()
			.queueName(QUEUE_NAME)
			.buildClient();
	}

	protected void configureDestination() {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", buildConnectionString());
		map.put("queue", QUEUE_NAME);
		config.setConfiguration(new MapConfiguration(map));
		config.initialize();
		destination.setConfig(config);
	}

	protected String receiveMessage() {
		var messages = receiverClient.receiveMessages(10, Duration.ofSeconds(5));
		var bodies = messages.stream().map(m -> m.getBody().toString()).toList();
		return bodies.isEmpty() ? null : bodies.get(0);
	}

	protected EventMessage createMessage(String eventId, String eventType, String contentType, byte[] eventBody) {
		return new EventMessage("test-realm", eventId, eventBody, eventType, contentType, "", "EVENT", "", "");
	}

	private String buildConnectionString() {
		return "Endpoint=sb://127.0.0.1:" + emulator.getMappedPort(AMQP_PORT)
			+ ";SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
	}

	private void waitForEmulatorReady() {
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				var tempClient = new ServiceBusClientBuilder()
					.connectionString(buildConnectionString())
					.receiver()
					.queueName(QUEUE_NAME)
					.buildClient();
				tempClient.peekMessage();
				tempClient.close();
				return true;
			} catch (Exception e) {
				return false;
			}
		});
	}
}
