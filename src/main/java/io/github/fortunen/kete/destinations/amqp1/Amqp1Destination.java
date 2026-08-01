package io.github.fortunen.kete.destinations.amqp1;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import org.apache.qpid.jms.JmsConnection;
import org.apache.qpid.jms.JmsSession;

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
	private String queueOrTopicName;
	private MessageProducer producer;
	private boolean isDestinationIsQueue;
	private boolean isQueueOrTopicNameTemplated;
	private jakarta.jms.Destination jmsDestination;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;
	private ConcurrentHashMap<String, jakarta.jms.Destination> destinationCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		queueOrTopicName = config.getQueueOrTopicName();
		isDestinationIsQueue = config.isDestinationIsQueue();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();
		isQueueOrTopicNameTemplated = config.isQueueOrTopicNameTemplated();

		// verify connection

		connection = config.getConnectionFactory().createConnection();
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(null);

		if (config.isHasPriority()) {
			producer.setPriority(config.getPriority());
		}

		producer.setDeliveryMode(config.getDeliveryMode());
		producer.setTimeToLive(config.getTimeToLiveSeconds() * 1000);

		if (!isQueueOrTopicNameTemplated) {
			jmsDestination = isDestinationIsQueue ? session.createQueue(queueOrTopicName) : session.createTopic(queueOrTopicName);
		}
	}

	@Override
	public boolean isHealthy() {

		if (ValidationUtils.isNull(connection) || ValidationUtils.isNull(session) || ValidationUtils.isNull(producer)) {
			return false;
		}

		// the jakarta.jms API exposes no state, but the Qpid JMS implementation does (atomic
		// flag reads); with no failover configured a dropped connection is terminal, so probe
		// the implementation when available and stay portable otherwise

		if (connection instanceof JmsConnection jmsConnection && (jmsConnection.isClosed() || jmsConnection.isFailed() || !jmsConnection.isConnected())) {
			return false;
		}

		return !(session instanceof JmsSession jmsSession) || !jmsSession.isClosed();
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// destination

		var actualQueueOrTopicName = isQueueOrTopicNameTemplated ? TemplateUtils.substitute(queueOrTopicName, message) : queueOrTopicName;

		var destination = isQueueOrTopicNameTemplated ? destinationCache.computeIfAbsent(actualQueueOrTopicName, name -> {
			try {
				return isDestinationIsQueue ? session.createQueue(name) : session.createTopic(name);
			} catch (JMSException exception) {
				throw new RuntimeException(exception);
			}
		}) : jmsDestination;

		// message

		var jmsMessage = session.createBytesMessage();

		jmsMessage.writeBytes(message.eventBody());

		for (var entry : customHeadersEntrySet) {
			jmsMessage.setStringProperty(entry.getKey(), entry.getValue());
		}

		jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		jmsMessage.setStringProperty(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// send

		producer.send(destination, jmsMessage);
	}

	@Override
	public void close() {
		destinationCache.clear();
		ValidationUtils.tryClose(producer, "producer");
		ValidationUtils.tryClose(session, "session");
		ValidationUtils.tryClose(connection, "connection");
	}
}
