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
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"producerConfiguration"})
public class KafkaDestinationConfig extends DestinationConfig {

	public static final String TOPIC = "topic";
	public static final int DEFAULT_LINGER_MS = 5;
	public static final int DEFAULT_BATCH_SIZE = 32768;
	public static final String DEFAULT_COMPRESSION_TYPE = "lz4";

	private static final String SASL_JAAS_CONFIG = "sasl.jaas.config";

	// Constructed at runtime to prevent Maven Shade Plugin from rewriting the value.
	// The shade plugin rewrites "org.apache.kafka." string constants to "kete.org.apache.kafka.",
	// which would make both constants identical and the config-rewrite comparison a no-op.
	private static final String KAFKA_PACKAGE_PREFIX = String.join(".", "org", "apache", "kafka") + ".";
	private static final String SHADED_KAFKA_PACKAGE_PREFIX = "kete.org.apache.kafka.";

	private String topic;
	private boolean transactional;
	private boolean isTopicTemplated;
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

		// sasl.jaas.config: rewrite unshaded Kafka class names to shaded equivalents

		var jaasConfig = producerConfiguration.getProperty(SASL_JAAS_CONFIG);

		if (ValidationUtils.isNotBlank(jaasConfig) && jaasConfig.contains(KAFKA_PACKAGE_PREFIX) && !jaasConfig.contains(SHADED_KAFKA_PACKAGE_PREFIX)) {
			producerConfiguration.put(SASL_JAAS_CONFIG, jaasConfig.replace(KAFKA_PACKAGE_PREFIX, SHADED_KAFKA_PACKAGE_PREFIX));
		}

		// defaults

		// pin the reliable delivery pair explicitly so a future kafka-clients default change
		// cannot silently weaken it; only when acks is not user-set, because an explicit
		// enable.idempotence=true combined with a user-chosen acks=1 would fail construction

		if (!producerConfiguration.containsKey(ProducerConfig.ACKS_CONFIG)) {
			producerConfiguration.put(ProducerConfig.ACKS_CONFIG, "all");
			producerConfiguration.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		}

		producerConfiguration.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, DEFAULT_LINGER_MS);
		producerConfiguration.putIfAbsent(ProducerConfig.BATCH_SIZE_CONFIG, DEFAULT_BATCH_SIZE);
		producerConfiguration.putIfAbsent(ProducerConfig.COMPRESSION_TYPE_CONFIG, DEFAULT_COMPRESSION_TYPE);
		producerConfiguration.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerConfiguration.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

		// tls

		if (tls.isEnabled()) {

			if (tls.isVerifyHostname()) {
				producerConfiguration.put("ssl.endpoint.identification.algorithm", "https");
			}

			var securityProtocol = ValidationUtils.requireNonBlank(producerConfiguration.getProperty("security.protocol"), ".destination.security.protocol is required").trim();

			ValidationUtils.requireTrue(securityProtocol.equals("SSL") || securityProtocol.equals("SASL_SSL"), ".destination.security.protocol must be 'SSL' or 'SASL_SSL' when TLS is enabled");

			if (ValidationUtils.isNotBlank(tls.getVersion())) {
				producerConfiguration.put("ssl.protocol", tls.getVersion());
			}

			if (ValidationUtils.isNotBlank(tls.getTrustStoreType())) {
				producerConfiguration.put("ssl.truststore.type", tls.getTrustStoreType());
			}

			producerConfiguration.put("ssl.truststore.location", tls.getTrustStoreFilePath());

			if (ValidationUtils.isNotBlank(tls.getTrustStorePassword())) {
				producerConfiguration.put("ssl.truststore.password", tls.getTrustStorePassword());
			}

			if (ValidationUtils.isNotBlank(tls.getTrustManagerAlgorithm())) {
				producerConfiguration.put("ssl.trustmanager.algorithm", tls.getTrustManagerAlgorithm());
			}

			if (tls.isKeyStoreConfigured()) {

				if (ValidationUtils.isNotBlank(tls.getKeyStoreType())) {
					producerConfiguration.put("ssl.keystore.type", tls.getKeyStoreType());
				}

				producerConfiguration.put("ssl.keystore.location", tls.getKeyStoreFilePath());

				if (ValidationUtils.isNotBlank(tls.getKeyStorePassword())) {
					producerConfiguration.put("ssl.keystore.password", tls.getKeyStorePassword());
				}

				if (ValidationUtils.isNotBlank(tls.getKeyPassword())) {
					producerConfiguration.put("ssl.key.password", tls.getKeyPassword());
				}

				if (ValidationUtils.isNotBlank(tls.getKeyManagerAlgorithm())) {
					producerConfiguration.put("ssl.keymanager.algorithm", tls.getKeyManagerAlgorithm());
				}
			}
		}

		// transactional

		transactional = producerConfiguration.containsKey(ProducerConfig.TRANSACTIONAL_ID_CONFIG);

		ValidationUtils.requireFalse(transactional, "transactional producers are not supported with connection pooling");

		// precomputed fields

		isTopicTemplated = TemplateUtils.containsTemplate(topic);

		// customHeadersBytes

		getCustomHeaders().forEach((key, value) -> customHeadersBytes.put(key, value.getBytes(StandardCharsets.UTF_8)));

		customHeadersBytesEntrySet = customHeadersBytes.entrySet();
	}
}
