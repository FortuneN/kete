package io.github.fortunen.kete.destinations.natsjetstream;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamOptions;
import io.nats.client.Nats;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@Component(name = "nats-jetstream")
@EqualsAndHashCode(callSuper = true)
public class NatsJetStreamDestination extends Destination<NatsJetStreamDestinationConfig> {

	private JetStream jetStream;
	private Connection connection;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		connection = Nats.connect(config.getNatsOptions());

		// verify connection

		ValidationUtils.requireTrue(connection.getStatus() == Connection.Status.CONNECTED, "failed to connect to NATS server");

		// verify stream exists

		var jsm = connection.jetStreamManagement();

		if (jsm.getStreamInfo(config.getStream()) == null) {
			throw new IllegalStateException("stream '" + config.getStream() + "' does not exist on the NATS server");
		}

		// jetStream

		var jsOptions = JetStreamOptions.builder()
			.publishNoAck(false)
			.requestTimeout(config.getPublishTimeout())
			.build();

		jetStream = connection.jetStream(jsOptions);
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// subject

		var actualSubject = TemplateUtils.substitute(config.getSubject(), message);

		// message

		var messageBuilder = NatsMessage.builder()
			.subject(actualSubject)
			.data(message.eventBody());

		// headers (message headers take priority over custom headers)

		var headers = new Headers();

		for (var entry : config.getCustomHeadersEntrySet()) {
			headers.add(entry.getKey(), entry.getValue());
		}

		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		headers.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		messageBuilder.headers(headers);

		// publish with ack (blocks until server acknowledges or timeout)

		jetStream.publish(messageBuilder.build());
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(connection, "connection");
	}
}
