package io.github.fortunen.kete.unittests.destinationconfigs.azureservicebusdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.azureservicebus.AzureServiceBusDestinationConfig;

public class initializeTests {

	private static final String VALID_CONNECTION_STRING = "Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

	private Map<String, Object> minimalQueueConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("queue", "test-queue");
		return map;
	}

	private Map<String, Object> minimalTopicConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("topic", "test-topic");
		return map;
	}

	// =========================================================================
	// Required Fields - Connection String
	// =========================================================================

	@Test
	public void shouldThrowWhenConnectionStringIsMissing() {

		// arrange

		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-servicebus", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsEmpty() {

		// arrange

		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-servicebus", "connection-string", "", "queue", "test-queue")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", "   ");
		map.put("queue", "test-queue");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	// =========================================================================
	// Required Fields - Queue or Topic
	// =========================================================================

	@Test
	public void shouldThrowWhenNeitherQueueNorTopicIsSet() {

		// arrange

		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-servicebus", "connection-string", VALID_CONNECTION_STRING)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("either 'queue' or 'topic' is required for azure-servicebus destinations");
	}

	@Test
	public void shouldThrowWhenBothQueueAndTopicAreSet() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("queue", "test-queue");
		map.put("topic", "test-topic");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'queue' and 'topic' are mutually exclusive — set one, not both");
	}

	@Test
	public void shouldThrowWhenQueueIsEmpty() {

		// arrange

		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-servicebus", "connection-string", VALID_CONNECTION_STRING, "queue", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("either 'queue' or 'topic' is required for azure-servicebus destinations");
	}

	@Test
	public void shouldThrowWhenTopicIsEmpty() {

		// arrange

		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-servicebus", "connection-string", VALID_CONNECTION_STRING, "topic", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("either 'queue' or 'topic' is required for azure-servicebus destinations");
	}

	@Test
	public void shouldAcceptQueueOnly() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueue()).isEqualTo("test-queue");
		assertThat(config.isHasQueue()).isTrue();
		assertThat(config.isHasTopic()).isFalse();
	}

	@Test
	public void shouldAcceptTopicOnly() {

		// arrange

		var map = minimalTopicConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("test-topic");
		assertThat(config.isHasTopic()).isTrue();
		assertThat(config.isHasQueue()).isFalse();
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldDefaultSubjectToEmpty() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSubject()).isEmpty();
		assertThat(config.isHasSubject()).isFalse();
	}

	@Test
	public void shouldDefaultSessionIdToEmpty() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSessionId()).isEmpty();
		assertThat(config.isHasSessionId()).isFalse();
	}

	@Test
	public void shouldDefaultCustomEndpointAddressToEmpty() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomEndpointAddress()).isEmpty();
		assertThat(config.isHasCustomEndpointAddress()).isFalse();
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalQueueConfig();
		map.put("timeout-seconds", 30);
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldUseCustomSubject() {

		// arrange

		var map = minimalQueueConfig();
		map.put("subject", "my-subject");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSubject()).isEqualTo("my-subject");
		assertThat(config.isHasSubject()).isTrue();
	}

	@Test
	public void shouldUseCustomSessionId() {

		// arrange

		var map = minimalQueueConfig();
		map.put("session-id", "session-1");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSessionId()).isEqualTo("session-1");
		assertThat(config.isHasSessionId()).isTrue();
	}

	@Test
	public void shouldUseCustomEndpointAddress() {

		// arrange

		var map = minimalQueueConfig();
		map.put("custom-endpoint-address", "http://localhost:5672");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomEndpointAddress()).isEqualTo("http://localhost:5672");
		assertThat(config.isHasCustomEndpointAddress()).isTrue();
	}

	@Test
	public void shouldTrimQueue() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("queue", "  test-queue  ");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getQueue()).isEqualTo("test-queue");
	}

	@Test
	public void shouldTrimTopic() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("topic", "  test-topic  ");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("test-topic");
	}

	@Test
	public void shouldTrimSubject() {

		// arrange

		var map = minimalQueueConfig();
		map.put("subject", "  my-subject  ");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSubject()).isEqualTo("my-subject");
	}

	@Test
	public void shouldTrimSessionId() {

		// arrange

		var map = minimalQueueConfig();
		map.put("session-id", "  session-1  ");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSessionId()).isEqualTo("session-1");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalQueueConfig();
		map.put("timeout-seconds", 0);
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldThrowWhenTimeoutSecondsIsNegative() {

		// arrange

		var map = minimalQueueConfig();
		map.put("timeout-seconds", -1);
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Validation - Custom Endpoint Address
	// =========================================================================

	@Test
	public void shouldThrowWhenCustomEndpointAddressIsInvalidUrl() {

		// arrange

		var map = minimalQueueConfig();
		map.put("custom-endpoint-address", "not-a-url");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("custom-endpoint-address must be a valid absolute URL");
	}

	// =========================================================================
	// Cross-Destination Property Rejection
	// =========================================================================

	@Test
	public void shouldThrowWhenStreamIsSet() {

		// arrange

		var map = minimalQueueConfig();
		map.put("stream", "some-stream");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' cannot be set for azure-servicebus destinations");
	}

	@Test
	public void shouldThrowWhenEventHubIsSet() {

		// arrange

		var map = minimalQueueConfig();
		map.put("event-hub", "some-hub");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'event-hub' cannot be set for azure-servicebus destinations");
	}

	@Test
	public void shouldThrowWhenPartitionKeyIsSet() {

		// arrange

		var map = minimalQueueConfig();
		map.put("partition-key", "some-key");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' cannot be set for azure-servicebus destinations");
	}

	@Test
	public void shouldThrowWhenPartitionIdIsSet() {

		// arrange

		var map = minimalQueueConfig();
		map.put("partition-id", "0");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-id' cannot be set for azure-servicebus destinations");
	}

	@Test
	public void shouldThrowWhenEndpointIsSet() {

		// arrange

		var map = minimalQueueConfig();
		map.put("endpoint", "https://some-endpoint.com");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'endpoint' cannot be set for azure-servicebus destinations");
	}

	@Test
	public void shouldThrowWhenAccessKeyIsSet() {

		// arrange

		var map = minimalQueueConfig();
		map.put("access-key", "some-key");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'access-key' cannot be set for azure-servicebus destinations");
	}

	// =========================================================================
	// Pre-computed Fields — Templating
	// =========================================================================

	@Test
	public void shouldDetectTemplatedQueue() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("queue", "events-${realm}");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isQueueTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedQueue() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isQueueTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedTopic() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("topic", "events-${realm}");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isTopicTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedTopic() {

		// arrange

		var map = minimalTopicConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isTopicTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedSubject() {

		// arrange

		var map = minimalQueueConfig();
		map.put("subject", "${eventType}");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSubjectTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedSubject() {

		// arrange

		var map = minimalQueueConfig();
		map.put("subject", "static-subject");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSubjectTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedSessionId() {

		// arrange

		var map = minimalQueueConfig();
		map.put("session-id", "${realm}-session");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSessionIdTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedSessionId() {

		// arrange

		var map = minimalQueueConfig();
		map.put("session-id", "static-session");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSessionIdTemplated()).isFalse();
	}

	// =========================================================================
	// Pre-computed Fields — Client Builder
	// =========================================================================

	@Test
	public void shouldSetServiceBusClientBuilder() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceBusClientBuilder()).isNotNull();
	}

	@Test
	public void shouldSetConnectionString() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionString()).isEqualTo(VALID_CONNECTION_STRING);
	}

	// =========================================================================
	// Pre-computed Fields — Service Endpoint (from connection string)
	// =========================================================================

	@Test
	public void shouldDeriveServiceEndpointHostFromConnectionString() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceEndpointHost()).isEqualTo("my-namespace.servicebus.windows.net");
	}

	@Test
	public void shouldUseDefaultAmqpsPortFromConnectionString() {

		// arrange

		var map = minimalQueueConfig();
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceEndpointPort()).isEqualTo(5671);
	}

	@Test
	public void shouldUseExplicitPortFromConnectionStringEndpoint() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", "Endpoint=sb://localhost:9093/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
		map.put("queue", "test-queue");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceEndpointHost()).isEqualTo("localhost");
		assertThat(config.getServiceEndpointPort()).isEqualTo(9093);
	}

	@Test
	public void shouldThrowWhenConnectionStringMissingEndpoint() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
		map.put("queue", "test-queue");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert (Azure SDK validates connection string before our endpoint parsing)

		assertThat(thrown).isNotNull();
	}

	// =========================================================================
	// Pre-computed Fields — Service Endpoint (from custom endpoint address)
	// =========================================================================

	@Test
	public void shouldDeriveServiceEndpointHostFromCustomEndpointAddress() {

		// arrange

		var map = minimalQueueConfig();
		map.put("custom-endpoint-address", "http://localhost:5672");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceEndpointHost()).isEqualTo("localhost");
	}

	@Test
	public void shouldUseExplicitPortFromCustomEndpointAddress() {

		// arrange

		var map = minimalQueueConfig();
		map.put("custom-endpoint-address", "http://localhost:9090");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceEndpointPort()).isEqualTo(9090);
	}

	@Test
	public void shouldUseDefaultAmqpPortFromCustomEndpointAddress() {

		// arrange

		var map = minimalQueueConfig();
		map.put("custom-endpoint-address", "http://my-emulator.local");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceEndpointHost()).isEqualTo("my-emulator.local");
		assertThat(config.getServiceEndpointPort()).isEqualTo(5672);
	}

	// =========================================================================
	// Trimming — Connection String
	// =========================================================================

	@Test
	public void shouldTrimConnectionString() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-servicebus");
		map.put("connection-string", "  " + VALID_CONNECTION_STRING + "  ");
		map.put("queue", "test-queue");
		var config = new AzureServiceBusDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionString()).isEqualTo(VALID_CONNECTION_STRING);
	}
}
