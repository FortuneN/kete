package io.github.fortunen.kete.destinations.stomp;

import java.util.HashMap;

import org.apache.activemq.transport.stomp.StompConnection;

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
@Component(name = "stomp")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class StompDestination extends Destination<StompDestinationConfig> {

	private StompConnection connection;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		connection = new StompConnection();

		var socket = config.getSocketFactory().createSocket(config.getHost(), config.getPort());

		socket.setSoTimeout(config.getReadTimeoutSeconds() * 1000);	connection.open(socket);

		var connectHeaders = new HashMap<String, String>();

		if (ValidationUtils.isNotBlank(config.getUsername())) {
			connectHeaders.put("login", config.getUsername());
		}

		if (ValidationUtils.isNotBlank(config.getPassword())) {
			connectHeaders.put("passcode", config.getPassword());
		}

		connectHeaders.put("accept-version", "1.1,1.2");

		if (ValidationUtils.isNotBlank(config.getVirtualHost())) {
			connectHeaders.put("host", config.getVirtualHost());
		}

		connectHeaders.put("heart-beat", config.getHeartBeatHeader());

		connection.connect(connectHeaders);
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var body = message.eventBody();
		var headers = new HashMap<String, String>();
		var actualDestination = TemplateUtils.substitute(config.getDestination(), message);

		for (var entry : config.getCustomHeadersEntrySet()) {
			headers.put(entry.getKey(), entry.getValue());
		}

		headers.put("content-type", message.contentType());
		headers.put("content-length", String.valueOf(body.length));
		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());

		if (config.isReceiptEnabled()) {
			headers.put("receipt", message.eventId());
		}

		connection.send(actualDestination, new String(body), null, headers);

		if (config.isReceiptEnabled()) {
			var receipt = connection.receive();
			ValidationUtils.requireTrue("RECEIPT".equals(receipt.getAction()), "STOMP receipt failed: " + receipt.getAction());
		}
	}

	@Override
	@SneakyThrows
	public void close() {
		ValidationUtils.tryClose(connection, "connection");
	}
}
