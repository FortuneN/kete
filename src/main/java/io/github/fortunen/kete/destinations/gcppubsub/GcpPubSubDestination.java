package io.github.fortunen.kete.destinations.gcppubsub;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpStatus;

import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.auth.http.HttpCredentialsAdapter;

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
@Component(name = "gcp-pubsub")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class GcpPubSubDestination extends Destination<GcpPubSubDestinationConfig> {

	private String topic;
	private Pubsub pubsub;
	private Duration timeout;
	private String orderingKey;
	private boolean hasOrderingKey;
	private boolean isTopicTemplated;
	private String publishTopicPrefix;
	private boolean isOrderingKeyTemplated;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;
	private final ConcurrentHashMap<String, String> topicNameCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		var credentials = config.getCredentials();
		var httpTransport = config.getHttpTransport();
		var initializer = ValidationUtils.isNotNull(credentials) ? new HttpCredentialsAdapter(credentials) : null;

		// build Pub/Sub client

		topic = config.getTopic();
		timeout = config.getTimeout();
		orderingKey = config.getOrderingKey();
		hasOrderingKey = config.isHasOrderingKey();
		isTopicTemplated = config.isTopicTemplated();
		publishTopicPrefix = config.getPublishTopicPrefix();
		isOrderingKeyTemplated = config.isOrderingKeyTemplated();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();
		pubsub = new Pubsub.Builder(httpTransport, GsonFactory.getDefaultInstance(), initializer).setApplicationName("kete").setRootUrl(config.getUrl() + "/").build();

		// verify connection

		try {

			if (isTopicTemplated) {
				pubsub.projects().topics().list("projects/" + config.getProject()).setPageSize(1).execute();
			} else {
				pubsub.projects().topics().get(publishTopicPrefix + topic).execute();
			}

		} catch (HttpResponseException exception) {

			if (exception.getStatusCode() != HttpStatus.SC_FORBIDDEN) {
				throw exception;
			}

			// connected but no read permission, no problem
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// topic

		var actualTopic = isTopicTemplated ? TemplateUtils.substitute(topic, message) : topic;

		// full topic name

		var fullTopicName = topicNameCache.computeIfAbsent(actualTopic, t -> publishTopicPrefix + t);

		// attributes

		var attributes = new HashMap<String, String>();

		for (var entry : customHeadersEntrySet) {
			attributes.put(entry.getKey(), entry.getValue());
		}

		attributes.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		attributes.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		attributes.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// message

		var pubsubMessage = new PubsubMessage()
			.encodeData(message.eventBody())
			.setAttributes(attributes);

		if (hasOrderingKey) {
			var actualOrderingKey = isOrderingKeyTemplated ? TemplateUtils.substitute(orderingKey, message) : orderingKey;
			pubsubMessage.setOrderingKey(actualOrderingKey);
		}

		// publish

		var messages = new ArrayList<PubsubMessage>();
		messages.add(pubsubMessage);

		var publishRequest = new PublishRequest().setMessages(messages);
		var response = pubsub.projects().topics().publish(fullTopicName, publishRequest).execute();

		ValidationUtils.requireTrue(response.getMessageIds() != null && !response.getMessageIds().isEmpty(), () -> new IOException("GCP Pub/Sub publish failed: no message IDs returned"));
	}

	@Override
	public void close() {
		// Pubsub client is stateless — no cleanup needed
	}
}
