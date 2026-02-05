package io.github.fortunen.kete.destinations.pulsar;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;

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
@Component(name = "pulsar")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class PulsarDestination extends Destination<PulsarDestinationConfig> {

	private PulsarClient client;
	private ConcurrentHashMap<String, Producer<byte[]>> producerCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		// client

		client = config.getClientBuilder().build();

		// verify connection

		client.getPartitionsForTopic(config.getTopic(), true).get(config.getOperationTimeoutSeconds(), TimeUnit.SECONDS);
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// producer

		var actualTopic = TemplateUtils.substitute(config.getTopic(), message);

		var producer = producerCache.computeIfAbsent(actualTopic, topic -> {
			try {

				var producerBuilder = client.newProducer()
					.topic(topic)
					.compressionType(config.getCompressionType())
					.blockIfQueueFull(config.isBlockIfQueueFull())
					.maxPendingMessages(config.getMaxPendingMessages())
					.batchingMaxMessages(config.getBatchingMaxMessages())
					.sendTimeout(config.getSendTimeoutSeconds(), TimeUnit.SECONDS)
					.batchingMaxPublishDelay(config.getBatchingMaxPublishDelay(), config.getBatchingMaxPublishDelayUnit());

				if (ValidationUtils.isNotBlank(config.getProducerName())) {
					producerBuilder.producerName(config.getProducerName());
				}

				return producerBuilder.create();

			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		});

		// message builder

		var messageBuilder = producer.newMessage()
			.value(message.eventBody())
			.key(message.eventType());

		// headers

		for (var entry : config.getCustomHeadersEntrySet()) {
			messageBuilder.property(entry.getKey(), entry.getValue());
		}

		messageBuilder
			.property(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind())
			.property(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType())
			.property(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// send

		messageBuilder.send();
	}

	@Override
	@SneakyThrows
	public void close() {
		producerCache.forEach((topic, producer) -> ValidationUtils.tryClose(producer, "producer for " + topic));
		producerCache.clear();
		ValidationUtils.tryClose(client, "client");
	}
}
