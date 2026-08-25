package io.github.fortunen.kete.destinations.awssns;

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
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Data
@Component(name = "aws-sns")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AwsSnsDestination extends Destination<AwsSnsDestinationConfig> {

	private String topic;
	private String subject;
	private String topicArn;
	private boolean hasSubject;
	private SnsClient snsClient;
	private String topicArnPrefix;
	private String messageGroupId;
	private boolean hasFifoGroupId;
	private boolean isTopicTemplated;
	private String messageDeduplicationId;
	private boolean hasFifoDeduplicationId;
	private Map<String, MessageAttributeValue> staticAttributes;

	@Override
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		topic = config.getTopic();
		subject = config.getSubject();
        topicArn = config.getTopicArn();
		hasSubject = config.isHasSubject();
		hasFifoGroupId = config.isHasFifoGroupId();
		topicArnPrefix = config.getTopicArnPrefix();
		messageGroupId = config.getMessageGroupId();
		isTopicTemplated = config.isTopicTemplated();
		staticAttributes = config.getStaticAttributes();
		snsClient = config.getSnsClientBuilder().build();
		hasFifoDeduplicationId = config.isHasFifoDeduplicationId();
		messageDeduplicationId = config.getMessageDeduplicationId();

		// verify connection

		snsClient.listTopics();
	}

	@Override
	public boolean isHealthy() {
		return true; // stateless HTTP client; nothing to probe
	}

	@Override
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// resolve topic ARN (skip template substitution if topic is static)

		var actualTopicArn = topicArn;

		if (isTopicTemplated) {
			var actualTopic = TemplateUtils.substitute(topic, message);
			if (!actualTopic.equals(topic)) {
				actualTopicArn = topicArnPrefix + actualTopic;
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

		var requestBuilder = PublishRequest.builder()
			.topicArn(actualTopicArn)
			.message(new String(payload, StandardCharsets.UTF_8))
			.messageAttributes(attributes);

		// subject (optional)

		if (hasSubject) {
			requestBuilder.subject(TemplateUtils.substitute(subject, message));
		}

		// FIFO properties

		if (hasFifoGroupId) {
			requestBuilder.messageGroupId(TemplateUtils.substitute(messageGroupId, message));
		}

		if (hasFifoDeduplicationId) {
			requestBuilder.messageDeduplicationId(TemplateUtils.substitute(messageDeduplicationId, message));
		}

        // publish

		snsClient.publish(requestBuilder.build());
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(snsClient, "snsClient");
	}
}
