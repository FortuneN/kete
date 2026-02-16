package io.github.fortunen.kete.destinations.pulsar;

import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.CompressionType;
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

	private String topic;
	private PulsarClient client;
	private String producerName;
	private int maxPendingMessages;
	private int sendTimeoutSeconds;
	private boolean hasSendTimeout;
	private boolean hasProducerName;
	private int batchingMaxMessages;
	private boolean hasBatchingMaxMessages;
	private boolean isTopicTemplated;
	private boolean blockIfQueueFull;
	private long batchingMaxPublishDelay;
	private CompressionType compressionType;
	private Producer<byte[]> defaultProducer;
	private TimeUnit batchingMaxPublishDelayUnit;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;
	private ConcurrentHashMap<String, Producer<byte[]>> producerCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		// fields

		topic = config.getTopic();
		producerName = config.getProducerName();
		client = config.getClientBuilder().build();
		isTopicTemplated = config.isTopicTemplated();
		hasProducerName = config.isHasProducerName();
		compressionType = config.getCompressionType();
		blockIfQueueFull = config.isBlockIfQueueFull();
		hasSendTimeout = config.isHasSendTimeout();
		sendTimeoutSeconds = config.getSendTimeoutSeconds();
		maxPendingMessages = config.getMaxPendingMessages();
		hasBatchingMaxMessages = config.isHasBatchingMaxMessages();
		batchingMaxMessages = config.getBatchingMaxMessages();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();
		batchingMaxPublishDelay = config.getBatchingMaxPublishDelay();
		batchingMaxPublishDelayUnit = config.getBatchingMaxPublishDelayUnit();

		if (!isTopicTemplated) {
			defaultProducer = createProducer(topic);
		}

		// verify connection

		client.getPartitionsForTopic(config.getTopic(), true).get(config.getOperationTimeoutSeconds(), TimeUnit.SECONDS);
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// producer

		var actualTopic = isTopicTemplated ? TemplateUtils.substitute(topic, message) : topic;

		var producer = isTopicTemplated ? producerCache.computeIfAbsent(actualTopic, this::createProducer) : defaultProducer;

		// message builder

		var messageBuilder = producer.newMessage()
			.value(message.eventBody())
			.key(message.eventType());

		// headers

		for (var entry : customHeadersEntrySet) {
			messageBuilder.property(entry.getKey(), entry.getValue());
		}

		messageBuilder
			.property(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind())
			.property(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType())
			.property(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// send

		messageBuilder.send();
	}

	@SneakyThrows
	private Producer<byte[]> createProducer(String topicName) {

		var producerBuilder = client.newProducer()
			.topic(topicName)
			.compressionType(compressionType)
			.blockIfQueueFull(blockIfQueueFull)
			.maxPendingMessages(maxPendingMessages)
			.batchingMaxPublishDelay(batchingMaxPublishDelay, batchingMaxPublishDelayUnit);

		if (hasSendTimeout) {
			producerBuilder.sendTimeout(sendTimeoutSeconds, TimeUnit.SECONDS);
		}

		if (hasBatchingMaxMessages) {
			producerBuilder.batchingMaxMessages(batchingMaxMessages);
		}

		if (hasProducerName) {
			producerBuilder.producerName(producerName);
		}

		return producerBuilder.create();
	}

	@Override
	@SneakyThrows
	public void close() {
		ValidationUtils.tryClose(defaultProducer, "producer");
		producerCache.forEach((topic, producer) -> ValidationUtils.tryClose(producer, "producer for " + topic));
		producerCache.clear();
		ValidationUtils.tryClose(client, "client");
	}
}
