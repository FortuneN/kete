package io.github.fortunen.kete.destinations.amqp1;

import java.util.concurrent.ConcurrentHashMap;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "amqp-1")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class Amqp1Destination extends Destination<Amqp1DestinationConfig> {

	private Session session;
	private Connection connection;
	private MessageProducer producer;
	private ConcurrentHashMap<String, jakarta.jms.Destination> destinationCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		connection = config.getConnectionFactory().createConnection();
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(null);

		producer.setPriority(config.getPriority());
		producer.setDeliveryMode(config.getDeliveryMode());
		producer.setTimeToLive(config.getTimeToLiveSeconds() * 1000);
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// destination

		var actualQueueOrTopicName = TemplateUtils.substitute(config.getQueueOrTopicName(), message);

		var jmsDestination = destinationCache.computeIfAbsent(actualQueueOrTopicName, name -> {
			try {
				return config.isDestinationIsQueue() ? session.createQueue(name) : session.createTopic(name);
			} catch (JMSException exception) {
				throw new RuntimeException(exception);
			}
		});

		// message

		var jmsMessage = session.createBytesMessage();

		jmsMessage.writeBytes(message.eventBody());

		for (var entry : config.getCustomHeadersEntrySet()) {
			jmsMessage.setStringProperty(entry.getKey(), entry.getValue());
		}

		jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// send

		producer.send(jmsDestination, jmsMessage);
	}

	@Override
	public void close() {
		destinationCache.clear();
		ValidationUtils.tryClose(producer, "producer");
		ValidationUtils.tryClose(session, "session");
		ValidationUtils.tryClose(connection, "connection");
	}
}
