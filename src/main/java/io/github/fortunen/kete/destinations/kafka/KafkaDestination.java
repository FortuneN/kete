package io.github.fortunen.kete.destinations.kafka;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "kafka")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class KafkaDestination extends Destination<KafkaDestinationConfig> {

	public static final int CLUSTER_DESCRIBE_TIMEOUT_SECONDS = 10;

	private String topic;
	private boolean isTopicTemplated;
	private KafkaProducer<String, byte[]> producer;
	private Set<Map.Entry<String, byte[]>> customHeadersBytesEntrySet;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		// JAAS LoginContext uses Thread Context ClassLoader to find LoginModule classes.
		// After shading, classes like PlainLoginModule live under kete.org.apache.kafka.*
		// but are only visible to the classloader that loaded this JAR (Keycloak's provider classloader).
		// We temporarily set the TCCL so JAAS can discover shaded LoginModule classes.

		var originalClassLoader = Thread.currentThread().getContextClassLoader();

		try {

			Thread.currentThread().setContextClassLoader(KafkaDestination.class.getClassLoader());

			producer = new KafkaProducer<>(config.getProducerConfiguration());

			topic = config.getTopic();
			isTopicTemplated = config.isTopicTemplated();
			customHeadersBytesEntrySet = config.getCustomHeadersBytesEntrySet();

			// verify connection

			try (var admin = AdminClient.create(config.getProducerConfiguration())) {
				admin.describeCluster().clusterId().get(Duration.ofSeconds(CLUSTER_DESCRIBE_TIMEOUT_SECONDS).toMillis(), TimeUnit.MILLISECONDS);
			}

		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	@Override
	public boolean isHealthy() {
		return true; // kafka-clients has no non-blocking health API (partitionsFor/flush block; metrics' connection-count legitimately reads 0 on idle-reaped connections); the producer self-heals and failures surface on send
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// topic

		var actualTopic = isTopicTemplated ? TemplateUtils.substitute(topic, message) : topic;

		// record

		var producerRecord = new ProducerRecord<String, byte[]>(actualTopic, message.eventType(), message.eventBody());

		// headers

		var headers = producerRecord.headers();

		for (var entry : customHeadersBytesEntrySet) {
			headers.add(entry.getKey(), entry.getValue());
		}

		headers.add(Constants.MESSAGE_HEADER_EVENT_KIND, message.kindBytes());
		headers.add(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventTypeBytes());
		headers.add(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentTypeBytes());

		// send

		producer.send(producerRecord).get();
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(producer, "producer");
	}
}
