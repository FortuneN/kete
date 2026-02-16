package io.github.fortunen.kete.unittests.destinationconfigs.awssnsdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.awssns.AwsSnsDestinationConfig;

public class initializeTests {

	private static final String VALID_ENDPOINT_URL = "http://localhost:4566";
	private static final String VALID_REGION = "us-east-1";
	private static final String VALID_ACCOUNT_ID = "123456789012";

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-sns");
		map.put("topic", "test-topic");
		map.put("region", VALID_REGION);
		map.put("endpoint-url", VALID_ENDPOINT_URL);
		return map;
	}

	private Map<String, Object> minimalRealAwsConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "aws-sns");
		map.put("topic", "test-topic");
		map.put("region", VALID_REGION);
		map.put("authentication-type", "access-key");
		map.put("access-key-id", "AKIAIOSFODNN7EXAMPLE");
		map.put("secret-access-key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
		map.put("account-id", VALID_ACCOUNT_ID);
		return map;
	}

	// =========================================================================
	// Required Fields - Topic
	// =========================================================================

	@Test
	public void shouldThrowWhenTopicIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("topic");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'topic' is required for aws-sns destinations");
	}

	@Test
	public void shouldThrowWhenTopicIsEmpty() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'topic' is required for aws-sns destinations");
	}

	@Test
	public void shouldThrowWhenTopicIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "   ");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'topic' is required for aws-sns destinations");
	}

	// =========================================================================
	// Required Fields - Region
	// =========================================================================

	@Test
	public void shouldThrowWhenRegionIsMissing() {

		// arrange

		var map = minimalConfig();
		map.remove("region");
		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("test-topic");
	}

	// =========================================================================
	// Required Fields - Account ID
	// =========================================================================

	@Test
	public void shouldThrowWhenAccountIdIsMissingForRealAws() {

		// arrange

		var map = minimalRealAwsConfig();
		map.remove("account-id");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("account-id is required");
	}

	@Test
	public void shouldDefaultAccountIdForEmulators() {

		// arrange

		var map = minimalConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAccountId()).isEqualTo(AwsSnsDestinationConfig.DEFAULT_EMULATOR_ACCOUNT_ID);
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(AwsSnsDestinationConfig.DEFAULT_TIMEOUT_SECONDS);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(AwsSnsDestinationConfig.DEFAULT_TIMEOUT_SECONDS));
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldTrimTopic() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "  test-topic  ");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("test-topic");
	}

	@Test
	public void shouldAcceptSubject() {

		// arrange

		var map = minimalConfig();
		map.put("subject", "my-subject");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isHasSubject()).isTrue();
		assertThat(config.getSubject()).isEqualTo("my-subject");
	}

	@Test
	public void shouldNotHaveSubjectByDefault() {

		// arrange

		var map = minimalConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isHasSubject()).isFalse();
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'queue' cannot be set for aws-sns destinations");
	}

	@Test
	public void shouldThrowWhenStreamIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("stream", "some-stream");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'stream' cannot be set for aws-sns destinations");
	}

	@Test
	public void shouldThrowWhenPartitionKeyIsSet() {

		// arrange

		var map = minimalConfig();
		map.put("partition-key", "some-key");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("'partition-key' cannot be set for aws-sns destinations");
	}

	// =========================================================================
	// FIFO Rules
	// =========================================================================

	@Test
	public void shouldThrowWhenMessageGroupIdSetButNotFifo() {

		// arrange

		var map = minimalConfig();
		map.put("message-group-id", "my-group");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("does not end in '.fifo'");
	}

	@Test
	public void shouldThrowWhenFifoTopicButNoMessageGroupId() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "test-topic.fifo");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("FIFO topics require a message group ID");
	}

	@Test
	public void shouldAcceptFifoTopicWithMessageGroupId() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "test-topic.fifo");
		map.put("message-group-id", "my-group");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isHasFifoGroupId()).isTrue();
		assertThat(config.getMessageGroupId()).isEqualTo("my-group");
	}

	@Test
	public void shouldAcceptFifoTopicWithDeduplicationId() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "test-topic.fifo");
		map.put("message-group-id", "my-group");
		map.put("message-deduplication-id", "{{eventId}}");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isHasFifoDeduplicationId()).isTrue();
		assertThat(config.getMessageDeduplicationId()).isEqualTo("{{eventId}}");
	}

	// =========================================================================
	// Topic ARN Construction
	// =========================================================================

	@Test
	public void shouldBuildTopicArnForEmulator() {

		// arrange

		var map = minimalConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopicArn()).isEqualTo("arn:aws:sns:" + VALID_REGION + ":" + AwsSnsDestinationConfig.DEFAULT_EMULATOR_ACCOUNT_ID + ":test-topic");
	}

	@Test
	public void shouldBuildTopicArnForRealAws() {

		// arrange

		var map = minimalRealAwsConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopicArn()).isEqualTo("arn:aws:sns:us-east-1:123456789012:test-topic");
	}

	// =========================================================================
	// Pre-computed Fields
	// =========================================================================

	@Test
	public void shouldSetSnsClientBuilder() {

		// arrange

		var map = minimalConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSnsClientBuilder()).isNotNull();
	}

	@Test
	public void shouldDetectTemplatedTopic() {

		// arrange

		var map = minimalConfig();
		map.put("topic", "events-${realm}");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isTopicTemplated()).isTrue();
	}

	@Test
	public void shouldDetectNonTemplatedTopic() {

		// arrange

		var map = minimalConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isTopicTemplated()).isFalse();
	}

	// =========================================================================
	// Static Attributes
	// =========================================================================

	@Test
	public void shouldBuildStaticAttributesFromConfig() {

		// arrange

		var map = minimalConfig();
		map.put("attributes.source", "keycloak");
		map.put("attributes.env", "prod");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getStaticAttributes()).containsKey("source");
		assertThat(config.getStaticAttributes().get("source").stringValue()).isEqualTo("keycloak");
		assertThat(config.getStaticAttributes()).containsKey("env");
		assertThat(config.getStaticAttributes().get("env").stringValue()).isEqualTo("prod");
	}

	@Test
	public void shouldIncludeCustomHeadersInStaticAttributes() {

		// arrange

		var map = minimalConfig();
		map.put("headers.x-custom", "my-value");
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getStaticAttributes()).containsKey("x-custom");
		assertThat(config.getStaticAttributes().get("x-custom").stringValue()).isEqualTo("my-value");
	}

	// =========================================================================
	// Authentication Type - Access Key
	// =========================================================================

	@Test
	public void shouldAcceptAccessKeyAuth() {

		// arrange

		var map = minimalRealAwsConfig();
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAuthenticationType()).isEqualTo("access-key");
		assertThat(config.getSnsClientBuilder()).isNotNull();
	}

	// =========================================================================
	// Authentication Type - Cross-Contamination
	// =========================================================================

	@Test
	public void shouldThrowWhenAccessKeyAuthHasCredentialsFilePath() {

		// arrange

		var map = minimalRealAwsConfig();
		map.put("credentials-file-path", "/some/path");
		var config = new AwsSnsDestinationConfig();
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
		map.put("account-id", VALID_ACCOUNT_ID);
		var config = new AwsSnsDestinationConfig();
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

		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
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

		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
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
		var config = new AwsSnsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-encoding: unknown");
	}
}
