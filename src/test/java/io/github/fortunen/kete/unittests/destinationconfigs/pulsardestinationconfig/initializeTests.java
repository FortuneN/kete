package io.github.fortunen.kete.unittests.destinationconfigs.pulsardestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.pulsar.PulsarDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Service URL
	// =========================================================================

	@Test
	public void shouldThrowWhenServiceUrlIsMissing() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"topic", "persistent://public/default/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("service-url is required");
	}

	@Test
	public void shouldThrowWhenServiceUrlIsEmpty() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "",
			"topic", "persistent://public/default/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("service-url is required");
	}

	@Test
	public void shouldThrowWhenServiceUrlIsBlank() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "   ",
			"topic", "persistent://public/default/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("service-url is required");
	}

	// =========================================================================
	// Required Fields - Topic
	// =========================================================================

	@Test
	public void shouldThrowWhenTopicIsMissing() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	@Test
	public void shouldThrowWhenTopicIsEmpty() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	@Test
	public void shouldThrowWhenTopicIsBlank() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	// =========================================================================
	// Success Cases
	// =========================================================================

	@Test
	public void shouldInitializeWithRequiredFieldsOnly() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getServiceUrl()).isEqualTo("pulsar://localhost:6650");
		assertThat(config.getTopic()).isEqualTo("persistent://public/default/events");
		assertThat(config.getSendTimeoutSeconds()).isEqualTo(0);
		assertThat(config.isHasSendTimeout()).isFalse();
		assertThat(config.getMaxPendingMessages()).isEqualTo(PulsarDestinationConfig.DEFAULT_MAX_PENDING_MESSAGES);
		assertThat(config.getBatchingMaxMessages()).isEqualTo(0);
		assertThat(config.isHasBatchingMaxMessages()).isFalse();
		assertThat(config.isBlockIfQueueFull()).isEqualTo(PulsarDestinationConfig.DEFAULT_BLOCK_IF_QUEUE_FULL);
		assertThat(config.getOperationTimeoutSeconds()).isEqualTo(PulsarDestinationConfig.DEFAULT_OPERATION_TIMEOUT_SECONDS);
		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(PulsarDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
		assertThat(config.getKeepAliveIntervalSeconds()).isEqualTo(PulsarDestinationConfig.DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS);
	}

	@Test
	public void shouldInitializeWithCustomSendTimeout() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"send-timeout-seconds", 60
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSendTimeoutSeconds()).isEqualTo(60);
	}

	@Test
	public void shouldInitializeWithCustomMaxPendingMessages() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"max-pending-messages", 5000
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getMaxPendingMessages()).isEqualTo(5000);
	}

	@Test
	public void shouldInitializeWithCustomBatchingMaxMessages() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"batching-max-messages", 2000
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getBatchingMaxMessages()).isEqualTo(2000);
	}

	@Test
	public void shouldDisableBatchingWhenNoBatchingOptionIsSet() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isBatchingEnabled()).isFalse();
	}

	@Test
	public void shouldEnableBatchingWhenBatchingMaxMessagesIsSet() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"batching-max-messages", 500
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isBatchingEnabled()).isTrue();
	}

	@Test
	public void shouldEnableBatchingWhenBatchingMaxPublishDelayIsSet() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"batching-max-publish-delay-seconds", 2
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isBatchingEnabled()).isTrue();
		assertThat(config.getBatchingMaxPublishDelay()).isEqualTo(2000L);
	}

	@Test
	public void shouldInitializeWithCustomBlockIfQueueFull() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"block-if-queue-full", false
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isBlockIfQueueFull()).isFalse();
	}

	@Test
	public void shouldInitializeWithCompressionType() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"compression-type", "ZSTD"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCompressionType().name()).isEqualTo("ZSTD");
	}

	@Test
	public void shouldInitializeWithProducerName() throws Exception {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"producer-name", "keycloak-producer"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerName()).isEqualTo("keycloak-producer");
	}

	// =========================================================================
	// Validation - Invalid Compression Type
	// =========================================================================

	@Test
	public void shouldThrowWhenCompressionTypeIsInvalid() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"compression-type", "INVALID"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("compression-type must be one of: NONE, LZ4, ZLIB, ZSTD, SNAPPY");
	}

	// =========================================================================
	// Validation - Batching Max Publish Delay
	// =========================================================================

	@Test
	public void shouldThrowWhenBatchingMaxPublishDelayIsZero() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"batching-max-publish-delay-seconds", 0
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("batching-max-publish-delay-seconds must be greater than 0");
	}

	@Test
	public void shouldThrowWhenBatchingMaxPublishDelayIsNegative() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"batching-max-publish-delay-seconds", -1
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("batching-max-publish-delay-seconds must be greater than 0");
	}

	// =========================================================================
	// Optional Fields - Username and Password
	// =========================================================================

	@Test
	public void shouldThrowWhenUsernameIsMissingWithBasicAuth() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "basic"
		)));

		// act

		var thrown = catchThrowable(config::initialize);

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("username is required when authentication-type is 'basic'");
	}

	@Test
	public void shouldDefaultToEmptyPassword() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "basic",
			"username", "pulsaruser"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEmpty();
	}

	@Test
	public void shouldAcceptUsernameAndPassword() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "basic",
			"username", "pulsaruser",
			"password", "pulsarpass"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("pulsaruser");
		assertThat(config.getPassword()).isEqualTo("pulsarpass");
	}

	@Test
	public void shouldTrimUsername() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "basic",
			"username", "  pulsaruser  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("pulsaruser");
	}

	@Test
	public void shouldTrimPassword() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "basic",
			"username", "pulsaruser",
			"password", "  pulsarpass  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEqualTo("pulsarpass");
	}

	// =========================================================================
	// Token
	// =========================================================================

	@Test
	public void shouldThrowWhenTokenIsMissingWithTokenAuth() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "token"
		)));

		// act

		var thrown = catchThrowable(config::initialize);

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("token is required when authentication-type is 'token'");
	}

	@Test
	public void shouldAcceptToken() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "token",
			"token", "eyJhbGciOiJIUzI1NiJ9.test"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiJ9.test");
	}

	@Test
	public void shouldTrimToken() {

		// arrange

		var config = new PulsarDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "pulsar",
			"service-url", "pulsar://localhost:6650",
			"topic", "persistent://public/default/events",
			"authentication-type", "token",
			"token", "  eyJhbGciOiJIUzI1NiJ9.test  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiJ9.test");
	}
}
