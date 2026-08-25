package io.github.fortunen.kete.unittests.destinationconfigs.azureeventhubsdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.azureeventhubs.AzureEventHubsDestinationConfig;

public class initializeTests {

	private static final String VALID_CONNECTION_STRING_WITH_ENTITY = "Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=;EntityPath=my-event-hub";
	private static final String VALID_CONNECTION_STRING = "Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventhubs");
		map.put("connection-string", VALID_CONNECTION_STRING_WITH_ENTITY);
		return map;
	}

	// =========================================================================
	// Required Fields - Connection String
	// =========================================================================

	@Test
	public void shouldThrowWhenConnectionStringIsMissing() {

		// arrange

		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-eventhubs")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsEmpty() {

		// arrange

		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-eventhubs", "connection-string", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventhubs");
		map.put("connection-string", "   ");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' or 'authentication-type' must be set");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(config.isHasTimeoutSeconds()).isFalse();
	}

	@Test
	public void shouldDefaultEventHubToEmpty() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventHub()).isEmpty();
		assertThat(config.isHasEventHub()).isFalse();
	}

	@Test
	public void shouldDefaultPartitionKeyToEmpty() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPartitionKey()).isEmpty();
		assertThat(config.isHasPartitionKey()).isFalse();
	}

	@Test
	public void shouldDefaultPartitionIdToEmpty() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPartitionId()).isEmpty();
		assertThat(config.isHasPartitionId()).isFalse();
	}

	@Test
	public void shouldDefaultCustomEndpointAddressToEmpty() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventHubsDestinationConfig();
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

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(config.isHasTimeoutSeconds()).isTrue();
	}

	@Test
	public void shouldUseCustomEventHub() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventhubs");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("event-hub", "my-hub");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventHub()).isEqualTo("my-hub");
		assertThat(config.isHasEventHub()).isTrue();
	}

	@Test
	public void shouldUseCustomPartitionKey() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "my-key");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPartitionKey()).isEqualTo("my-key");
		assertThat(config.isHasPartitionKey()).isTrue();
	}

	@Test
	public void shouldUseCustomPartitionId() {

		// arrange

		var map = minimalConfig();
		map.put("partition-id", "0");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPartitionId()).isEqualTo("0");
		assertThat(config.isHasPartitionId()).isTrue();
	}

	@Test
	public void shouldUseCustomEndpointAddress() {

		// arrange

		var map = minimalConfig();
		map.put("custom-endpoint-address", "http://localhost:5672");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomEndpointAddress()).isEqualTo("http://localhost:5672");
		assertThat(config.isHasCustomEndpointAddress()).isTrue();
	}

	@Test
	public void shouldTrimEventHub() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventhubs");
		map.put("connection-string", VALID_CONNECTION_STRING);
		map.put("event-hub", "  my-hub  ");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventHub()).isEqualTo("my-hub");
	}

	@Test
	public void shouldTrimPartitionKey() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "  my-key  ");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPartitionKey()).isEqualTo("my-key");
	}

	@Test
	public void shouldTrimPartitionId() {

		// arrange

		var map = minimalConfig();
		map.put("partition-id", "  0  ");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPartitionId()).isEqualTo("0");
	}

	@Test
	public void shouldTrimCustomEndpointAddress() {

		// arrange

		var map = minimalConfig();
		map.put("custom-endpoint-address", "  http://localhost:5672  ");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomEndpointAddress()).isEqualTo("http://localhost:5672");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldThrowWhenTimeoutSecondsIsNegative() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", -1);
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Validation - Partition Key / Partition ID Mutual Exclusivity
	// =========================================================================

	@Test
	public void shouldThrowWhenBothPartitionKeyAndPartitionIdAreSet() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "my-key");
		map.put("partition-id", "0");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' and 'partition-id' are mutually exclusive — set one or neither, not both");
	}

	// =========================================================================
	// Validation - Custom Endpoint Address
	// =========================================================================

	@Test
	public void shouldThrowWhenCustomEndpointAddressIsInvalidUrl() {

		// arrange

		var map = minimalConfig();
		map.put("custom-endpoint-address", "not-a-url");
		var config = new AzureEventHubsDestinationConfig();
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
	public void shouldThrowWhenQueueIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("queue", "some-queue");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'queue' cannot be set for azure-eventhubs destinations");
	}

	@Test
	public void shouldThrowWhenTopicIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "some-topic");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'topic' cannot be set for azure-eventhubs destinations");
	}

	@Test
	public void shouldThrowWhenStreamIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "some-stream");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' cannot be set for azure-eventhubs destinations");
	}

	@Test
	public void shouldThrowWhenSubjectIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("subject", "some-subject");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'subject' cannot be set for azure-eventhubs destinations");
	}

	@Test
	public void shouldThrowWhenEndpointIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("endpoint", "https://some-endpoint.com");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'endpoint' cannot be set for azure-eventhubs destinations");
	}

	@Test
	public void shouldThrowWhenAccessKeyIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("access-key", "some-key");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'access-key' cannot be set for azure-eventhubs destinations");
	}

	// =========================================================================
	// Pre-computed Fields — Templating
	// =========================================================================

	@Test
	public void shouldDetectTemplatedPartitionKey() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "${realm}-partition");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isPartitionKeyTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedPartitionKey() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "static-key");
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isPartitionKeyTemplated()).isFalse();
	}

	// =========================================================================
	// Pre-computed Fields — Client Builder
	// =========================================================================

	@Test
	public void shouldSetEventHubClientBuilder() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventHubClientBuilder()).isNotNull();
	}

	@Test
	public void shouldSetConnectionString() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventHubsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionString()).isEqualTo(VALID_CONNECTION_STRING_WITH_ENTITY);
	}
}
