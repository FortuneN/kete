package io.github.fortunen.kete.destinations.nats;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.impl.Headers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "nats")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class NatsDestination extends Destination<NatsDestinationConfig> {

	private Connection connection;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		connection = Nats.connect(config.getNatsOptions());

		// verify connection

		ValidationUtils.requireTrue(connection.getStatus() == Connection.Status.CONNECTED, "failed to connect to NATS server");
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// subject

		var actualSubject = TemplateUtils.substitute(config.getSubject(), message);

		// headers (message headers take priority over custom headers)

		var headers = new Headers();

		for (var entry : config.getCustomHeadersEntrySet()) {
			headers.add(entry.getKey(), entry.getValue());
		}

		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		headers.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// publish

		connection.publish(actualSubject, headers, message.eventBody());
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(connection, "connection");
	}
}
