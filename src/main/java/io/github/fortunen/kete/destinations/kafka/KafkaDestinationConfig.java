package io.github.fortunen.kete.destinations.kafka;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class KafkaDestinationConfig extends DestinationConfig {

	public static final String TOPIC = "topic";

	public static final int DEFAULT_LINGER_MS = 5;
	public static final String DEFAULT_ACKS = "all";
	public static final int DEFAULT_MAX_IN_FLIGHT = 5;
	public static final int DEFAULT_BATCH_SIZE = 32768;
	public static final String DEFAULT_COMPRESSION_TYPE = "lz4";
	public static final boolean DEFAULT_ENABLE_IDEMPOTENCE = true;

	private String topic;
	private boolean transactional;
	private Properties producerConfiguration;
	private Set<Map.Entry<String, byte[]>> customHeadersBytesEntrySet;
	private Map<String, byte[]> customHeadersBytes = new LinkedHashMap<>();

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// producerConfiguration

		producerConfiguration = new Properties();

		configuration.getKeys().forEachRemaining(key -> {
			var value = configuration.getProperty(key);
			if (ValidationUtils.isNotNull(value)) {
				producerConfiguration.put(key, value);
			}
		});

		// bootstrapServers

		ValidationUtils.requireNonBlank(producerConfiguration.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG), ProducerConfig.BOOTSTRAP_SERVERS_CONFIG + " is required");

		// topic

		ValidationUtils.requireNonBlank(producerConfiguration.getProperty(TOPIC), TOPIC + " is required");

		topic = producerConfiguration.getProperty(TOPIC).trim();

		producerConfiguration.remove(TOPIC);

		// defaults

		producerConfiguration.putIfAbsent(ProducerConfig.ACKS_CONFIG, DEFAULT_ACKS);
		producerConfiguration.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, DEFAULT_LINGER_MS);
		producerConfiguration.putIfAbsent(ProducerConfig.BATCH_SIZE_CONFIG, DEFAULT_BATCH_SIZE);
		producerConfiguration.putIfAbsent(ProducerConfig.COMPRESSION_TYPE_CONFIG, DEFAULT_COMPRESSION_TYPE);
		producerConfiguration.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, DEFAULT_ENABLE_IDEMPOTENCE);
		producerConfiguration.putIfAbsent(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, DEFAULT_MAX_IN_FLIGHT);
		producerConfiguration.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerConfiguration.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

		// tls

		if (tls.isEnabled()) {
			tls = TlsMaterial.builder().withConfiguration(ConfigurationUtils.getSubSet(configuration, TLS)).withWriteFiles(true).build();
			tls.updateKafkaConfiguration(producerConfiguration);
		}

		// transactional

		transactional = producerConfiguration.containsKey(ProducerConfig.TRANSACTIONAL_ID_CONFIG);

		ValidationUtils.requireFalse(transactional, "transactional producers are not supported with connection pooling");

		// customHeadersBytes

		getCustomHeaders().forEach((key, value) -> {
			customHeadersBytes.put(key, value.getBytes(StandardCharsets.UTF_8));
		});

		customHeadersBytesEntrySet = customHeadersBytes.entrySet();
	}
}
