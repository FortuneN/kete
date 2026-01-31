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

		if (config.isMessageHeadersEnabled()) {

			var headers = new HashMap<String, Object>();

			builder.contentType(message.contentType());
			headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
			headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());

			builder.headers(headers);
		}

		// delivery mode

		builder.deliveryMode(config.getDeliveryMode());

		// priority

		builder.priority(config.getPriority());

		// time-to-live (expiration)

		if (config.isHasTimeToLive() && config.getTimeToLive() > 0) {
			builder.expiration(String.valueOf(config.getTimeToLive()));
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
