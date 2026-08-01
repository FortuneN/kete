package io.github.fortunen.kete.destinations.amqp091;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

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
@Component(name = "amqp-0.9.1")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class Amqp091Destination extends Destination<Amqp091DestinationConfig> {

	private int priority;
	private Channel channel;
	private String exchange;
	private int deliveryMode;
	private String routingKey;
	private Connection connection;
	private boolean hasPriority;
	private boolean publisherConfirms;
	private long confirmTimeoutMillis;
	private String timeToLiveExpiration;
	private boolean isExchangeTemplated;
	private boolean isRoutingKeyTemplated;
	private Map<String, Object> staticHeaders;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		exchange = config.getExchange();
		priority = config.getPriority();
		routingKey = config.getRoutingKey();
		deliveryMode = config.getDeliveryMode();
		hasPriority = config.isHasPriority();
		staticHeaders = config.getStaticHeaders();
		isExchangeTemplated = config.isExchangeTemplated();
		isRoutingKeyTemplated = config.isRoutingKeyTemplated();
		timeToLiveExpiration = config.getTimeToLiveExpiration();
		publisherConfirms = config.isPublisherConfirms();
		confirmTimeoutMillis = config.getConfirmTimeoutSeconds() * 1000L;

		// verify connection

		connection = config.getConnectionFactory().newConnection();
		channel = connection.createChannel();

		if (publisherConfirms) {
			channel.confirmSelect();
		}
	}

	@Override
	public boolean isHealthy() {
		return ValidationUtils.isNotNull(connection) && connection.isOpen() && ValidationUtils.isNotNull(channel) && channel.isOpen();
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var builder = new AMQP.BasicProperties.Builder();
		var actualExchange = isExchangeTemplated ? TemplateUtils.substitute(exchange, message) : exchange;
		var actualRoutingKey = isRoutingKeyTemplated ? TemplateUtils.substitute(routingKey, message) : routingKey;

		// headers (start from precomputed static headers)

		builder.contentType(message.contentType());

		var headers = new HashMap<>(staticHeaders);

		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());

		builder.headers(headers);

		// deliveryMode

		builder.deliveryMode(deliveryMode);

		// priority

		if (hasPriority) {
			builder.priority(priority);
		}

		// timeToLiveSeconds

		if (ValidationUtils.isNotNull(timeToLiveExpiration)) {
			builder.expiration(timeToLiveExpiration);
		}

		// publish

		channel.basicPublish(actualExchange, actualRoutingKey, builder.build(), message.eventBody());

		// without confirms basicPublish is fire-and-forget; block until the broker
		// acknowledges (throws on nack or timeout)

		if (publisherConfirms) {
			channel.waitForConfirmsOrDie(confirmTimeoutMillis);
		}
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(channel, "channel");
		ValidationUtils.tryClose(connection, "connection");
	}
}
