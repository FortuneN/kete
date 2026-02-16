package io.github.fortunen.kete.unittests.destinationconfigs.kafkadestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().containsKey(ProducerConfig.ACKS_CONFIG)).isFalse();
	}

	@Test
	public void shouldDefaultLingerMsToFive() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().containsKey(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isFalse();
	}

	@Test
	public void shouldDefaultMaxInFlightRequestsToFive() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "kafka",
			"bootstrap.servers", "localhost:9092",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().containsKey(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)).isFalse();
	}

	@Test
	public void shouldDefaultKeySerializerToStringSerializer() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
	// tls Configuration
	// =========================================================================

	@Test
	public void shouldDefaultToTlsDisabled() {

		// arrange

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
			"kind", "kafka",
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
		configMap.put("kind", "kafka");
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
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "broker1:9092,broker2:9092");
		configMap.put("topic", "keycloak-events");
		configMap.put("acks", "1");
		configMap.put("linger.ms", "50");
		configMap.put("batch.size", "65536");
		configMap.put("compression.type", "gzip");
		configMap.put("enable.idempotence", "false");
		configMap.put("max.in.flight.requests.per.connection", "10");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getTopic()).isEqualTo("keycloak-events");
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

	// =========================================================================
	// Custom Headers Bytes
	// =========================================================================

	@Test
	public void shouldConvertCustomHeadersToBytes() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("headers.X-Custom-Header", "custom-value");
		configMap.put("headers.Authorization", "Bearer token123");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeadersBytes())
			.containsEntry("X-Custom-Header", "custom-value".getBytes(StandardCharsets.UTF_8))
			.containsEntry("Authorization", "Bearer token123".getBytes(StandardCharsets.UTF_8))
			.hasSize(2);
	}

	@Test
	public void shouldPopulateCustomHeadersBytesEntrySet() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("headers.X-Header-1", "value1");
		configMap.put("headers.X-Header-2", "value2");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeadersBytesEntrySet())
			.isNotNull()
			.hasSize(2);

		var keys = config.getCustomHeadersBytesEntrySet().stream()
			.map(Map.Entry::getKey)
			.toList();

		assertThat(keys).containsExactlyInAnyOrder("X-Header-1", "X-Header-2");
	}

	@Test
	public void shouldHaveEmptyCustomHeadersBytesWhenNoHeaders() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeadersBytes()).isEmpty();
		assertThat(config.getCustomHeadersBytesEntrySet()).isEmpty();
	}

	@Test
	public void shouldHandleUtf8InCustomHeadersBytes() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("headers.X-Unicode", "héllo wörld 中文");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		var expectedBytes = "héllo wörld 中文".getBytes(StandardCharsets.UTF_8);
		assertThat(config.getCustomHeadersBytes().get("X-Unicode")).isEqualTo(expectedBytes);
	}

	@Test
	public void shouldNotIncludeReservedHeadersInCustomHeadersBytes() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("headers.eventkind", "should-be-filtered");
		configMap.put("headers.eventtype", "should-be-filtered");
		configMap.put("headers.contenttype", "should-be-filtered");
		configMap.put("headers.X-Custom", "allowed");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeadersBytes())
			.doesNotContainKey("eventkind")
			.doesNotContainKey("eventtype")
			.doesNotContainKey("contenttype")
			.containsKey("X-Custom")
			.hasSize(1);
	}

	// =========================================================================
	// SASL JAAS Config - Shade-Aware Class Name Rewriting
	// Users always configure standard (unshaded) class names like
	// org.apache.kafka.common.security.plain.PlainLoginModule.
	// At runtime, KETE rewrites them to the shaded equivalents so that
	// JAAS LoginContext can find the classes inside the shaded JAR.
	// =========================================================================

	@Test
	public void shouldRewriteStandardKafkaClassNameInSaslJaasConfig() {

		// arrange - user provides standard (unshaded) class name, as documented

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("security.protocol", "SASL_PLAINTEXT");
		configMap.put("sasl.mechanism", "PLAIN");
		configMap.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"pass\";");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert - internally rewritten to shaded equivalent

		assertThat(config.getProducerConfiguration().getProperty("sasl.jaas.config"))
			.startsWith("kete.org.apache.kafka.common.security.plain.PlainLoginModule");
	}

	@Test
	public void shouldNotDoubleRewriteIfShadedPrefixAlreadyPresent() {

		// arrange - defensive: if someone somehow provides the shaded name, don't double-prefix it

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("security.protocol", "SASL_PLAINTEXT");
		configMap.put("sasl.mechanism", "PLAIN");
		configMap.put("sasl.jaas.config", "kete.org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"pass\";");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert - no double-prefixing (kete.kete.)

		assertThat(config.getProducerConfiguration().getProperty("sasl.jaas.config"))
			.startsWith("kete.org.apache.kafka.common.security.plain.PlainLoginModule")
			.doesNotContain("kete.kete.");
	}

	@Test
	public void shouldNotRewriteNonKafkaLoginModuleInSaslJaasConfig() {

		// arrange - non-Kafka login modules (e.g. Kerberos) must be left untouched

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "kafka");
		configMap.put("bootstrap.servers", "localhost:9092");
		configMap.put("topic", "test-topic");
		configMap.put("security.protocol", "SASL_PLAINTEXT");
		configMap.put("sasl.mechanism", "PLAIN");
		configMap.put("sasl.jaas.config", "com.sun.security.auth.module.Krb5LoginModule required useKeyTab=true;");

		var config = new KafkaDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getProducerConfiguration().getProperty("sasl.jaas.config"))
			.startsWith("com.sun.security.auth.module.Krb5LoginModule")
			.doesNotContain("kete.");
	}
}
