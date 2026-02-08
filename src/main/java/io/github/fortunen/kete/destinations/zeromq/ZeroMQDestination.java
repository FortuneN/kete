package io.github.fortunen.kete.destinations.zeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "zeromq")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class ZeroMQDestination extends Destination<ZeroMQDestinationConfig> {

	private Socket socket;
	private ZContext context;

	@Override
	@SneakyThrows
	public void doInitialize() {

		// context

		context = new ZContext(1);

		// socket

		socket = context.createSocket(config.getSocketTypeValue());

		// linger

		ValidationUtils.requireTrue(socket.setLinger(config.getLinger()), "failed to set linger");

		// bind or connect

		if (config.getConnectionMode().equals(ZeroMQDestinationConfig.BIND)) {
			ValidationUtils.requireTrue(socket.bind(config.getEndpoint()), "failed to bind to " + config.getEndpoint());
		} else {
			ValidationUtils.requireTrue(socket.connect(config.getEndpoint()), "failed to connect to " + config.getEndpoint());
		}
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		socket.send(message.eventBody());
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(socket, "socket");
		ValidationUtils.tryClose(context, "context");
	}
}