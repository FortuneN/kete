package io.github.fortunen.kete.destinations.amqp1;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import jakarta.jms.Connection;
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

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		connection = config.getConnectionFactory().createConnection();
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(null);

		producer.setPriority(config.getPriority());
		producer.setTimeToLive(config.getTimeToLive());
		producer.setDeliveryMode(config.getDeliveryMode());
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// destination

		var actualQueueOrTopicName = TemplateUtils.substitute(config.getQueueOrTopicName(), message);
		var jmsDestination = config.isDestinationIsQueue() ? session.createQueue(actualQueueOrTopicName) : session.createTopic(actualQueueOrTopicName);

		// message

		var jmsMessage = session.createBytesMessage();

		jmsMessage.writeBytes(message.eventBody());

		if (config.isMessageHeadersEnabled()) {
			jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());
			jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
			jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		}

		// send

		producer.send(jmsDestination, jmsMessage);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(producer, "producer");
		ValidationUtils.tryClose(session, "session");
		ValidationUtils.tryClose(connection, "connection");
	}
}
