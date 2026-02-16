package io.github.fortunen.kete.unittests.destinationconfigs.awskinesisdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.awskinesis.AwsKinesisDestinationConfig;

public class initializeTests {

	private static final String VALID_ENDPOINT_URL = "http://localhost:4566";
	private static final String VALID_REGION = "us-east-1";

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-kinesis");
		map.put("stream", "test-stream");
		map.put("partition-key", "${eventType}");
		map.put("region", VALID_REGION);
		map.put("endpoint-url", VALID_ENDPOINT_URL);
		return map;
	}

	private Map<String, Object> minimalRealAwsConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-kinesis");
		map.put("stream", "test-stream");
		map.put("partition-key", "${eventType}");
		map.put("region", VALID_REGION);
		map.put("authentication-type", "access-key");
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		return map;
	}

	// =========================================================================
	// Required Fields - Stream
	// =========================================================================

	@Test
	public void shouldThrowWhenStreamIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("stream");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' is required for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenStreamIsEmpty() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' is required for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenStreamIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "   ");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' is required for aws-kinesis destinations");
	}

	// =========================================================================
	// Required Fields - Partition Key
	// =========================================================================

	@Test
	public void shouldThrowWhenPartitionKeyIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("partition-key");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' is required for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenPartitionKeyIsEmpty() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' is required for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenPartitionKeyIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "   ");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' is required for aws-kinesis destinations");
	}

	// =========================================================================
	// Required Fields - Region
	// =========================================================================

	@Test
	public void shouldThrowWhenRegionIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("region");
		var config = new AwsKinesisDestinationConfig();
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
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getStream()).isEqualTo("test-stream");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(AwsKinesisDestinationConfig.DEFAULT_TIMEOUT_SECONDS);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(AwsKinesisDestinationConfig.DEFAULT_TIMEOUT_SECONDS));
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldTrimStream() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "  test-stream  ");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getStream()).isEqualTo("test-stream");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new AwsKinesisDestinationConfig();
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
		var config = new AwsKinesisDestinationConfig();
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
		var config = new AwsKinesisDestinationConfig();
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
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'queue' cannot be set for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenTopicIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "some-topic");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'topic' cannot be set for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenSubjectIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("subject", "some-subject");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'subject' cannot be set for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenMessageGroupIdIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("message-group-id", "some-group");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'message-group-id' cannot be set for aws-kinesis destinations");
	}

	@Test
	public void shouldThrowWhenMessageDeduplicationIdIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("message-deduplication-id", "some-dedup-id");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'message-deduplication-id' cannot be set for aws-kinesis destinations");
	}

	// =========================================================================
	// Pre-computed Fields
	// =========================================================================

	@Test
	public void shouldSetKinesisClientBuilder() {

		// arrange

		var map = minimalConfig();
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getKinesisClientBuilder()).isNotNull();
	}

	@Test
	public void shouldDetectTemplatedStream() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "events-${realm}");
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isStreamTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedStream() {

		// arrange

		var map = minimalConfig();
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isStreamTemplated()).isFalse();
	}

	@Test
	public void shouldDetectTemplatedPartitionKey() {

		// arrange

		var map = minimalConfig();
		var config = new AwsKinesisDestinationConfig();
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
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isPartitionKeyTemplated()).isFalse();
	}

	// =========================================================================
	// Authentication Type - Access Key
	// =========================================================================

	@Test
	public void shouldAcceptAccessKeyAuth() {

		// arrange

		var map = minimalRealAwsConfig();
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAuthenticationType()).isEqualTo("access-key");
		assertThat(config.getKinesisClientBuilder()).isNotNull();
	}

	// =========================================================================
	// Authentication Type - Cross-Contamination
	// =========================================================================

	@Test
	public void shouldThrowWhenAccessKeyAuthHasCredentialsFilePath() {

		// arrange

		var map = minimalRealAwsConfig();
		map.put("credentials-file-path", "/some/path");
		var config = new AwsKinesisDestinationConfig();
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
		var config = new AwsKinesisDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown authentication-type: 'unknown-auth'");
	}
}
