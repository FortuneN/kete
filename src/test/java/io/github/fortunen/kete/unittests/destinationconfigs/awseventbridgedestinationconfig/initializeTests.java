package io.github.fortunen.kete.unittests.destinationconfigs.awseventbridgedestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.awseventbridge.AwsEventBridgeDestinationConfig;

public class initializeTests {

	private static final String VALID_ENDPOINT_URL = "http://localhost:4566";
	private static final String VALID_REGION = "us-east-1";

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-eventbridge");
		map.put("event-bus", "test-bus");
		map.put("source", "kete.keycloak");
		map.put("detail-type", "KeycloakEvent");
		map.put("region", VALID_REGION);
		map.put("endpoint-url", VALID_ENDPOINT_URL);
		return map;
	}

	private Map<String, Object> minimalRealAwsConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-eventbridge");
		map.put("event-bus", "test-bus");
		map.put("source", "kete.keycloak");
		map.put("detail-type", "KeycloakEvent");
		map.put("region", VALID_REGION);
		map.put("authentication-type", "access-key");
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		return map;
	}

	// =========================================================================
	// Required Fields - Event Bus
	// =========================================================================

	@Test
	public void shouldThrowWhenEventBusIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("event-bus");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'event-bus' is required for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenEventBusIsEmpty() {

		// arrange

		var map = minimalConfig();
		map.put("event-bus", "");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'event-bus' is required for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenEventBusIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("event-bus", "   ");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'event-bus' is required for aws-eventbridge destinations");
	}

	// =========================================================================
	// Required Fields - Source
	// =========================================================================

	@Test
	public void shouldThrowWhenSourceIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("source");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'source' is required for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenSourceIsEmpty() {

		// arrange

		var map = minimalConfig();
		map.put("source", "");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'source' is required for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenSourceIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("source", "   ");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'source' is required for aws-eventbridge destinations");
	}

	// =========================================================================
	// Required Fields - Detail Type
	// =========================================================================

	@Test
	public void shouldThrowWhenDetailTypeIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("detail-type");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'detail-type' is required for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenDetailTypeIsEmpty() {

		// arrange

		var map = minimalConfig();
		map.put("detail-type", "");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'detail-type' is required for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenDetailTypeIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("detail-type", "   ");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'detail-type' is required for aws-eventbridge destinations");
	}

	// =========================================================================
	// Required Fields - Region
	// =========================================================================

	@Test
	public void shouldThrowWhenRegionIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("region");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("region is required");
	}

	// =========================================================================
	// Authentication Type - Optional
	// =========================================================================

	@Test
	public void shouldAllowOmittedAuthenticationTypeWhenEndpointUrlIsSet() {

		// arrange

		var map = minimalConfig();
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventBus()).isEqualTo("test-bus");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(AwsEventBridgeDestinationConfig.DEFAULT_TIMEOUT_SECONDS);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(AwsEventBridgeDestinationConfig.DEFAULT_TIMEOUT_SECONDS));
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldTrimEventBus() {

		// arrange

		var map = minimalConfig();
		map.put("event-bus", "  test-bus  ");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventBus()).isEqualTo("test-bus");
	}

	@Test
	public void shouldTrimSource() {

		// arrange

		var map = minimalConfig();
		map.put("source", "  kete.keycloak  ");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSource()).isEqualTo("kete.keycloak");
	}

	@Test
	public void shouldTrimDetailType() {

		// arrange

		var map = minimalConfig();
		map.put("detail-type", "  KeycloakEvent  ");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getDetailType()).isEqualTo("KeycloakEvent");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new AwsEventBridgeDestinationConfig();
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
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Validation - Endpoint URL
	// =========================================================================

	@Test
	public void shouldThrowWhenEndpointUrlIsInvalid() {

		// arrange

		var map = minimalConfig();
		map.put("endpoint-url", "not-a-url");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("endpoint-url must be a valid absolute URL");
	}

	// =========================================================================
	// Cross-Destination Property Rejection
	// =========================================================================

	@Test
	public void shouldThrowWhenQueueIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("queue", "some-queue");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'queue' cannot be set for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenTopicIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "some-topic");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'topic' cannot be set for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenStreamIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "some-stream");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' cannot be set for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenSubjectIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("subject", "some-subject");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'subject' cannot be set for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenPartitionKeyIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "some-key");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' cannot be set for aws-eventbridge destinations");
	}

	@Test
	public void shouldThrowWhenConnectionStringIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("connection-string", "some-connection-string");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'connection-string' cannot be set for aws-eventbridge destinations");
	}

	// =========================================================================
	// Pre-computed Fields
	// =========================================================================

	@Test
	public void shouldSetEventBridgeClientBuilder() {

		// arrange

		var map = minimalConfig();
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventBridgeClientBuilder()).isNotNull();
	}

	@Test
	public void shouldDetectTemplatedEventBus() {

		// arrange

		var map = minimalConfig();
		map.put("event-bus", "events-${realm}");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isEventBusTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedEventBus() {

		// arrange

		var map = minimalConfig();
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isEventBusTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedDetailType() {

		// arrange

		var map = minimalConfig();
		map.put("detail-type", "${eventType}");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isDetailTypeTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedDetailType() {

		// arrange

		var map = minimalConfig();
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isDetailTypeTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedSource() {

		// arrange

		var map = minimalConfig();
		map.put("source", "kete.${realm}");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSourceTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedSource() {

		// arrange

		var map = minimalConfig();
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isSourceTemplated()).isFalse();
	}

	// =========================================================================
	// Authentication Type - Access Key
	// =========================================================================

	@Test
	public void shouldAcceptAccessKeyAuth() {

		// arrange

		var map = minimalRealAwsConfig();
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAuthenticationType()).isEqualTo("access-key");
		assertThat(config.getEventBridgeClientBuilder()).isNotNull();
	}

	// =========================================================================
	// Authentication Type - Cross-Contamination
	// =========================================================================

	@Test
	public void shouldThrowWhenAccessKeyAuthHasCredentialsFilePath() {

		// arrange

		var map = minimalRealAwsConfig();
		map.put("credentials-file-path", "/some/path");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("'credentials-file-path' cannot be set when authentication-type is 'access-key'");
	}

	// =========================================================================
	// Authentication Type - Unknown
	// =========================================================================

	@Test
	public void shouldThrowWhenAuthenticationTypeIsUnknown() {

		// arrange

		var map = minimalConfig();
		map.remove("endpoint-url");
		map.put("authentication-type", "unknown-auth");
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown authentication-type: 'unknown-auth'");
	}

	// =========================================================================
	// content-transfer-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentTransferEncodingToNull() {

		// arrange

		var config = new AwsEventBridgeDestinationConfig();
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
		var config = new AwsEventBridgeDestinationConfig();
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
		var config = new AwsEventBridgeDestinationConfig();
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

		var config = new AwsEventBridgeDestinationConfig();
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
		var config = new AwsEventBridgeDestinationConfig();
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
		var config = new AwsEventBridgeDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-encoding: unknown");
	}
}
