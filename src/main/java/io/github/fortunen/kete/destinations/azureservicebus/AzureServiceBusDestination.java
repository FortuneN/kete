package io.github.fortunen.kete.destinations.azureservicebus;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

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
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "azure-servicebus")
public class AzureServiceBusDestination extends Destination<AzureServiceBusDestinationConfig> {

	private String queue;
	private String topic;
	private String subject;
	private String sessionId;
	private boolean hasQueue;
	private boolean hasTopic;
	private int timeoutMillis;
	private boolean hasSubject;
	private boolean hasSessionId;
	private int serviceEndpointPort;
	private boolean isQueueTemplated;
	private boolean isTopicTemplated;
	private boolean isSubjectTemplated;
	private String serviceEndpointHost;
	private boolean isSessionIdTemplated;
	private ServiceBusSenderClient senderClient;
	private ServiceBusClientBuilder serviceBusClientBuilder;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;
	private final ConcurrentHashMap<String, ServiceBusSenderClient> senderClientCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		queue = config.getQueue();
		topic = config.getTopic();
		subject = config.getSubject();
		hasQueue = config.isHasQueue();
		hasTopic = config.isHasTopic();
		sessionId = config.getSessionId();
		hasSubject = config.isHasSubject();
		hasSessionId = config.isHasSessionId();
		isQueueTemplated = config.isQueueTemplated();
		isTopicTemplated = config.isTopicTemplated();
		isSubjectTemplated = config.isSubjectTemplated();
		isSessionIdTemplated = config.isSessionIdTemplated();
		timeoutMillis = (int) config.getTimeout().toMillis();
		serviceEndpointHost = config.getServiceEndpointHost();
		serviceEndpointPort = config.getServiceEndpointPort();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();
		serviceBusClientBuilder = config.getServiceBusClientBuilder();

		// build sender client (for non-templated entity names)

		if (hasQueue && !isQueueTemplated) {
			senderClient = serviceBusClientBuilder.sender().queueName(queue).buildClient();
		} else if (hasTopic && !isTopicTemplated) {
			senderClient = serviceBusClientBuilder.sender().topicName(topic).buildClient();
		}

		// verify connection (TCP — Service Bus uses AMQP protocol)

		try (var socket = new Socket()) {
			socket.connect(new InetSocketAddress(serviceEndpointHost, serviceEndpointPort), timeoutMillis);
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// resolve sender client (use cached client for templated entity names)

		ServiceBusSenderClient client;

		if (hasQueue) {
			client = isQueueTemplated ? senderClientCache.computeIfAbsent(TemplateUtils.substitute(queue, message), q -> serviceBusClientBuilder.sender().queueName(q).buildClient()) : senderClient;
		} else {
			client = isTopicTemplated ? senderClientCache.computeIfAbsent(TemplateUtils.substitute(topic, message), t -> serviceBusClientBuilder.sender().topicName(t).buildClient()) : senderClient;
		}

		// build service bus message

		var sbMessage = new ServiceBusMessage(message.eventBody());
		sbMessage.setContentType(message.contentType());

		// set application properties (standard headers)

		sbMessage.getApplicationProperties().put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		sbMessage.getApplicationProperties().put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		sbMessage.getApplicationProperties().put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// set custom headers

		for (var entry : customHeadersEntrySet) {
			sbMessage.getApplicationProperties().put(entry.getKey(), entry.getValue());
		}

		// set subject (optional, templatable)

		if (hasSubject) {
			var actualSubject = isSubjectTemplated ? TemplateUtils.substitute(subject, message) : subject;
			sbMessage.setSubject(actualSubject);
		}

		// set session-id (optional, templatable)

		if (hasSessionId) {
			var actualSessionId = isSessionIdTemplated ? TemplateUtils.substitute(sessionId, message) : sessionId;
			sbMessage.setSessionId(actualSessionId);
		}

		// send

		client.sendMessage(sbMessage);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(senderClient, "ServiceBusSenderClient");
		senderClientCache.values().forEach(c -> ValidationUtils.tryClose(c, "ServiceBusSenderClient"));
		senderClientCache.clear();
	}
}
