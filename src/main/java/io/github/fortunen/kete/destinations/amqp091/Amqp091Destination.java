package io.github.fortunen.kete.destinations.amqp091;

import java.util.HashMap;

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

	private Channel channel;
	private Connection connection;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		connection = config.getConnectionFactory().newConnection();
		channel = connection.createChannel();
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var builder = new AMQP.BasicProperties.Builder();
		var actualExchange = TemplateUtils.substitute(config.getExchange(), message);
		var actualRoutingKey = TemplateUtils.substitute(config.getRoutingKey(), message);

		// headers

		builder.contentType(message.contentType());

		var headers = new HashMap<String, Object>();

		for (var entry : config.getCustomHeadersEntrySet()) {
			headers.put(entry.getKey(), entry.getValue());
		}

		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());

		builder.headers(headers);

		// deliveryMode

		builder.deliveryMode(config.getDeliveryMode());

		// priority

		builder.priority(config.getPriority());

		// timeToLiveSeconds

		if (config.isHasTimeToLiveSeconds() && config.getTimeToLiveSeconds() > 0) {
			builder.expiration(String.valueOf(config.getTimeToLiveSeconds() * 1000));
		}

		// publish

		channel.basicPublish(actualExchange, actualRoutingKey, builder.build(), message.eventBody());
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(channel, "channel");
		ValidationUtils.tryClose(connection, "connection");
	}
}
