package io.github.fortunen.kete.destinations.kafka;

import java.time.Duration;
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

	private KafkaProducer<String, byte[]> producer;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		producer = new KafkaProducer<>(config.getProducerConfiguration());

		// test

		try (var admin = AdminClient.create(config.getProducerConfiguration())) {
			admin.describeCluster().clusterId().get(Duration.ofSeconds(CLUSTER_DESCRIBE_TIMEOUT_SECONDS).toMillis(), TimeUnit.MILLISECONDS);
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// topic

		var actualTopic = TemplateUtils.substitute(config.getTopic(), message);

		// record

		var producerRecord = new ProducerRecord<String, byte[]>(actualTopic, message.eventType(), message.eventBody());

		// headers

		var headers = producerRecord.headers();

		for (var entry : config.getCustomHeadersBytesEntrySet()) {
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
