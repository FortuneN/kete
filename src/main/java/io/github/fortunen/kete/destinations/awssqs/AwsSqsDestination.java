package io.github.fortunen.kete.destinations.awssqs;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Data
@Component(name = "aws-sqs")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AwsSqsDestination extends Destination<AwsSqsDestinationConfig> {

	private String queue;
	private String queueUrl;
	private SqsClient sqsClient;
	private String queueUrlBase;
	private String messageGroupId;
	private boolean hasFifoGroupId;
	private boolean isQueueTemplated;
	private String messageDeduplicationId;
	private boolean hasFifoDeduplicationId;
	private Map<String, MessageAttributeValue> staticAttributes;

	@Override
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

        queue = config.getQueue();
		queueUrl = config.getQueueUrl();
		queueUrlBase = config.getQueueUrlBase();
		hasFifoGroupId = config.isHasFifoGroupId();
		messageGroupId = config.getMessageGroupId();
		isQueueTemplated = config.isQueueTemplated();
		staticAttributes = config.getStaticAttributes();
		sqsClient = config.getSqsClientBuilder().build();
		hasFifoDeduplicationId = config.isHasFifoDeduplicationId();
		messageDeduplicationId = config.getMessageDeduplicationId();

		// verify connection

		sqsClient.listQueues(r -> r.maxResults(1));
	}

	@Override
	public boolean isHealthy() {
		return true; // stateless HTTP client; nothing to probe
	}

	@Override
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// resolve queue URL (skip template substitution if queue is static)

		var actualQueueUrl = queueUrl;

		if (isQueueTemplated) {
			var actualQueue = TemplateUtils.substitute(queue, message);
			if (!actualQueue.equals(queue)) {
				actualQueueUrl = queueUrlBase + actualQueue;
			}
		}

		// message attributes (start from precomputed static attributes)

		var attributes = new HashMap<>(staticAttributes);

		// built-in KETE attributes (always win on key conflict)

		attributes.put(Constants.MESSAGE_HEADER_EVENT_KIND, MessageAttributeValue.builder().dataType("String").stringValue(message.kind()).build());
		attributes.put(Constants.MESSAGE_HEADER_EVENT_TYPE, MessageAttributeValue.builder().dataType("String").stringValue(message.eventType()).build());
		attributes.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, MessageAttributeValue.builder().dataType("String").stringValue(message.contentType()).build());

		// build request

		var payload = encodePayload(message.eventBody());

		var requestBuilder = SendMessageRequest.builder()
			.queueUrl(actualQueueUrl)
			.messageBody(new String(payload, StandardCharsets.UTF_8))
			.messageAttributes(attributes);

		// FIFO properties

		if (hasFifoGroupId) {
			requestBuilder.messageGroupId(TemplateUtils.substitute(messageGroupId, message));
		}

		if (hasFifoDeduplicationId) {
			requestBuilder.messageDeduplicationId(TemplateUtils.substitute(messageDeduplicationId, message));
		}

        // send

		sqsClient.sendMessage(requestBuilder.build());
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(sqsClient, "sqsClient");
	}
}
