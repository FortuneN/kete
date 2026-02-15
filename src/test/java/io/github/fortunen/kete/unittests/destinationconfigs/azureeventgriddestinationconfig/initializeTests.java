package io.github.fortunen.kete.unittests.destinationconfigs.azureeventgriddestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.azureeventgrid.AzureEventGridDestinationConfig;

public class initializeTests {

	private static final String VALID_ENDPOINT = "https://my-topic.westus2-1.eventgrid.azure.net/api/events";
	private static final String VALID_ACCESS_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventgrid");
		map.put("endpoint", VALID_ENDPOINT);
		map.put("access-key", VALID_ACCESS_KEY);
		return map;
	}

	// =========================================================================
	// Required Fields - Endpoint
	// =========================================================================

	@Test
	public void shouldThrowWhenEndpointIsMissing() {

		// arrange

		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-eventgrid", "access-key", VALID_ACCESS_KEY)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("endpoint is required");
	}

	@Test
	public void shouldThrowWhenEndpointIsEmpty() {

		// arrange

		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-eventgrid", "endpoint", "", "access-key", VALID_ACCESS_KEY)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("endpoint is required");
	}

	@Test
	public void shouldThrowWhenEndpointIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventgrid");
		map.put("endpoint", "   ");
		map.put("access-key", VALID_ACCESS_KEY);
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("endpoint is required");
	}

	@Test
	public void shouldThrowWhenEndpointIsInvalidUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventgrid");
		map.put("endpoint", "not-a-url");
		map.put("access-key", VALID_ACCESS_KEY);
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("endpoint must be a valid absolute URL");
	}

	// =========================================================================
	// Required Fields - Access Key
	// =========================================================================

	@Test
	public void shouldThrowWhenAccessKeyIsMissing() {

		// arrange

		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-eventgrid", "endpoint", VALID_ENDPOINT)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'access-key' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenAccessKeyIsEmpty() {

		// arrange

		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "azure-eventgrid", "endpoint", VALID_ENDPOINT, "access-key", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'access-key' or 'authentication-type' must be set");
	}

	@Test
	public void shouldThrowWhenAccessKeyIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "azure-eventgrid");
		map.put("endpoint", VALID_ENDPOINT);
		map.put("access-key", "   ");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'access-key' or 'authentication-type' must be set");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
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

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSubject()).isEmpty();
		assertThat(config.isHasSubject()).isFalse();
	}

	@Test
	public void shouldDefaultEventTypeToKeycloakEvent() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventType()).isEqualTo("KeycloakEvent");
	}

	@Test
	public void shouldDefaultDataVersionToOnePointZero() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getDataVersion()).isEqualTo("1.0");
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new AzureEventGridDestinationConfig();
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

		var map = minimalConfig();
		map.put("subject", "my-subject");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSubject()).isEqualTo("my-subject");
		assertThat(config.isHasSubject()).isTrue();
	}

	@Test
	public void shouldUseCustomEventType() {

		// arrange

		var map = minimalConfig();
		map.put("event-type", "CustomEventType");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventType()).isEqualTo("CustomEventType");
	}

	@Test
	public void shouldUseCustomDataVersion() {

		// arrange

		var map = minimalConfig();
		map.put("data-version", "2.0");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getDataVersion()).isEqualTo("2.0");
	}

	@Test
	public void shouldTrimEndpoint() {

		// arrange

		var map = minimalConfig();
		map.put("endpoint", "  " + VALID_ENDPOINT + "  ");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEndpoint()).isEqualTo(VALID_ENDPOINT);
	}

	@Test
	public void shouldTrimSubject() {

		// arrange

		var map = minimalConfig();
		map.put("subject", "  my-subject  ");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSubject()).isEqualTo("my-subject");
	}

	@Test
	public void shouldTrimEventType() {

		// arrange

		var map = minimalConfig();
		map.put("event-type", "  CustomEventType  ");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventType()).isEqualTo("CustomEventType");
	}

	@Test
	public void shouldTrimDataVersion() {

		// arrange

		var map = minimalConfig();
		map.put("data-version", "  2.0  ");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getDataVersion()).isEqualTo("2.0");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new AzureEventGridDestinationConfig();
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
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Validation - Event Type / Data Version Cannot Be Blank
	// =========================================================================

	@Test
	public void shouldThrowWhenEventTypeIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("event-type", "   ");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("event-type cannot be blank");
	}

	@Test
	public void shouldThrowWhenDataVersionIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("data-version", "   ");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("data-version cannot be blank");
	}

	// =========================================================================
	// Cross-Destination Property Rejection
	// =========================================================================

	@Test
	public void shouldThrowWhenQueueIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("queue", "some-queue");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'queue' cannot be set for azure-eventgrid destinations");
	}

	@Test
	public void shouldThrowWhenTopicIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "some-topic");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'topic' cannot be set for azure-eventgrid destinations");
	}

	@Test
	public void shouldThrowWhenStreamIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "some-stream");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' cannot be set for azure-eventgrid destinations");
	}

	@Test
	public void shouldThrowWhenEventHubIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("event-hub", "some-hub");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'event-hub' cannot be set for azure-eventgrid destinations");
	}

	@Test
	public void shouldThrowWhenPartitionKeyIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "some-key");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' cannot be set for azure-eventgrid destinations");
	}

	@Test
	public void shouldThrowWhenPartitionIdIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("partition-id", "0");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-id' cannot be set for azure-eventgrid destinations");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("connection-string", "some-connection-string");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' cannot be set for azure-eventgrid destinations");
	}

	@Test
	public void shouldThrowWhenSessionIdIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("session-id", "session-1");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'session-id' cannot be set for azure-eventgrid destinations");
	}

	// =========================================================================
	// Pre-computed Fields — Templating
	// =========================================================================

	@Test
	public void shouldDetectTemplatedSubject() {

		// arrange

		var map = minimalConfig();
		map.put("subject", "${eventType}");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSubjectTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedSubject() {

		// arrange

		var map = minimalConfig();
		map.put("subject", "static-subject");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSubjectTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedEventType() {

		// arrange

		var map = minimalConfig();
		map.put("event-type", "Keycloak.${eventType}");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isEventTypeTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedEventType() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isEventTypeTemplated()).isFalse();
	}

	// =========================================================================
	// Pre-computed Fields — Client Builder / HTTP Client
	// =========================================================================

	@Test
	public void shouldSetHttpClient() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHttpClient()).isNotNull();
	}

	@Test
	public void shouldSetEventGridClientBuilder() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventGridClientBuilder()).isNotNull();
	}

	@Test
	public void shouldSetEndpoint() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEndpoint()).isEqualTo(VALID_ENDPOINT);
	}

	@Test
	public void shouldSetAccessKey() {

		// arrange

		var map = minimalConfig();
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAccessKey()).isEqualTo(VALID_ACCESS_KEY);
	}

	// =========================================================================
	// content-transfer-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentTransferEncodingToNull() {

		// arrange

		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNull();
		assertThat(config.getContentTransferEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentTransferEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-transfer-encoding", "base64");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNotNull();
		assertThat(config.getContentTransferEncodingName()).isEqualTo("base64");
	}

	@Test
	public void shouldThrowWhenContentTransferEncodingIsUnknown() {

		// arrange

		var map = minimalConfig();
		map.put("content-transfer-encoding", "unknown");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-transfer-encoding: unknown");
	}

	// =========================================================================
	// content-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentEncodingToNull() {

		// arrange

		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNull();
		assertThat(config.getContentEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentEncoding() {

		// arrange

		var map = minimalConfig();
		map.put("content-encoding", "gzip");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNotNull();
		assertThat(config.getContentEncodingName()).isEqualTo("gzip");
	}

	@Test
	public void shouldThrowWhenContentEncodingIsUnknown() {

		// arrange

		var map = minimalConfig();
		map.put("content-encoding", "unknown");
		var config = new AzureEventGridDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-encoding: unknown");
	}
}
