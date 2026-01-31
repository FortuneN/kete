package io.github.fortunen.kete.unittests.destinantionconfigs.kafkadestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.kafka.KafkaDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Bootstrap Servers
	// =========================================================================

	@Test
	public void shouldThrowWhenBootstrapServersIsMissing() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"topic", "test-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("bootstrap.servers is required");
	}

	@Test
	public void shouldThrowWhenBootstrapServersIsEmpty() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "",
			"topic", "test-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("bootstrap.servers is required");
	}

	@Test
	public void shouldThrowWhenBootstrapServersIsBlank() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "   ",
			"topic", "test-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("bootstrap.servers is required");
	}

	// =========================================================================
	// Required Fields - Topic
	// =========================================================================

	@Test
	public void shouldThrowWhenTopicIsMissing() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092"
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

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
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

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
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
	// Successful Initialization
	// =========================================================================

	@Test
	public void shouldInitializeWithMinimumRequiredFields() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("test-topic");
		assertThat(config.getProducerConfiguration()).isNotNull();
		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
	}

	@Test
	public void shouldTrimTopic() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "  test-topic  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("test-topic");
	}

	@Test
	public void shouldRemoveTopicFromProducerConfiguration() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().containsKey("topic")).isFalse();
	}

	// =========================================================================
	// Default Producer Configuration
	// =========================================================================

	@Test
	public void shouldDefaultAcksToAll() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
	}

	@Test
	public void shouldDefaultLingerMsToFive() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(5);
	}

	@Test
	public void shouldDefaultBatchSizeTo32768() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().get(ProducerConfig.BATCH_SIZE_CONFIG)).isEqualTo(32768);
	}

	@Test
	public void shouldDefaultCompressionTypeToLz4() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG)).isEqualTo("lz4");
	}

	@Test
	public void shouldDefaultEnableIdempotenceToTrue() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
	}

	@Test
	public void shouldDefaultMaxInFlightRequestsToFive() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)).isEqualTo(5);
	}

	@Test
	public void shouldDefaultKeySerializerToStringSerializer() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class.getName());
	}

	@Test
	public void shouldDefaultValueSerializerToByteArraySerializer() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(ByteArraySerializer.class.getName());
	}

	// =========================================================================
	// Custom Producer Configuration
	// =========================================================================

	@Test
	public void shouldOverrideDefaultAcks() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic",
			"acks", "1"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.ACKS_CONFIG)).isEqualTo("1");
	}

	@Test
	public void shouldOverrideDefaultLingerMs() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic",
			"linger.ms", "100"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo("100");
	}

	@Test
	public void shouldOverrideDefaultCompressionType() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic",
			"compression.type", "snappy"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG)).isEqualTo("snappy");
	}

	@Test
	public void shouldPassThroughCustomProducerProperties() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic",
			"buffer.memory", "67108864",
			"max.block.ms", "60000"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty("buffer.memory")).isEqualTo("67108864");
		assertThat(config.getProducerConfiguration().getProperty("max.block.ms")).isEqualTo("60000");
	}

	// =========================================================================
	// Message Headers
	// =========================================================================

	@Test
	public void shouldDefaultToMessageHeadersEnabled() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isMessageHeadersEnabled()).isTrue();
	}

	@Test
	public void shouldDisableMessageHeaders() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic",
			"message-headers-enabled", "false"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isMessageHeadersEnabled()).isFalse();
	}

	// =========================================================================
	// TLS Configuration
	// =========================================================================

	@Test
	public void shouldDefaultToTlsDisabled() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isFalse();
	}

	@Test
	public void shouldEnableTls() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic",
			"tls.enabled", "true",
			"security.protocol", "SSL"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
	}

	// =========================================================================
	// Transactional Producer - Not Supported
	// =========================================================================

	@Test
	public void shouldThrowWhenTransactionalIdIsProvided() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic",
			"transactional.id", "my-transactional-id"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("transactional producers are not supported with connection pooling");
	}

	// =========================================================================
	// Multiple Bootstrap Servers
	// =========================================================================

	@Test
	public void shouldAcceptMultipleBootstrapServers() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"bootstrap.servers", "broker1:9092,broker2:9092,broker3:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("broker1:9092,broker2:9092,broker3:9092");
	}

	// =========================================================================
	// Null Values Filtered
	// =========================================================================

	@Test
	public void shouldFilterNullValues() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("null-property", null);

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().containsKey("null-property")).isFalse();
	}

	// =========================================================================
	// Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithFullConfiguration() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("bootstrap.servers", "broker1:9092,broker2:9092");
		configMap.put("topic", "keycloak-events");
		configMap.put("acks", "1");
		configMap.put("linger.ms", "50");
		configMap.put("batch.size", "65536");
		configMap.put("compression.type", "gzip");
		configMap.put("enable.idempotence", "false");
		configMap.put("max.in.flight.requests.per.connection", "10");
		configMap.put("message-headers-enabled", "false");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("keycloak-events");
		assertThat(config.isMessageHeadersEnabled()).isFalse();
		assertThat(config.isTransactional()).isFalse();

		var props = config.getProducerConfiguration();
		assertThat(props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("broker1:9092,broker2:9092");
		assertThat(props.getProperty(ProducerConfig.ACKS_CONFIG)).isEqualTo("1");
		assertThat(props.getProperty("linger.ms")).isEqualTo("50");
		assertThat(props.getProperty("batch.size")).isEqualTo("65536");
		assertThat(props.getProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG)).isEqualTo("gzip");
		assertThat(props.getProperty("enable.idempotence")).isEqualTo("false");
		assertThat(props.getProperty("max.in.flight.requests.per.connection")).isEqualTo("10");
		assertThat(props.containsKey("topic")).isFalse();
	}
}
