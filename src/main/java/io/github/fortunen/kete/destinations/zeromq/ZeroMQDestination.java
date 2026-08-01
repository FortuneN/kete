package io.github.fortunen.kete.destinations.zeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
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
	private String envelope;
	private ZContext context;
	private boolean hasEnvelope;
	private boolean isEnvelopeTemplated;

	@Override
	@SneakyThrows
	public void doInitialize() {

		// context

		context = new ZContext(1);

		// socket

		socket = context.createSocket(config.getSocketTypeValue());

		// linger

		ValidationUtils.requireTrue(socket.setLinger(config.getLinger()), "failed to set linger");

		// send-high-water-mark

		if (config.isHasSendHighWaterMark()) {
			ValidationUtils.requireTrue(socket.setSndHWM(config.getSendHighWaterMark()), "failed to set send-high-water-mark");
		}

		// CURVE

		if (config.isCurveEnabled()) {
			socket.setCurvePublicKey(config.getCurvePublicKey());
			socket.setCurveSecretKey(config.getCurveSecretKey());
			socket.setCurveServerKey(config.getCurveServerKey());
		}

		// envelope

		hasEnvelope = config.isHasEnvelope();
		if (hasEnvelope) {
			envelope = config.getEnvelope();
			isEnvelopeTemplated = config.isEnvelopeTemplated();
		}

		// verify connection

		if (config.getConnectionMode().equals(ZeroMQDestinationConfig.BIND)) {
			ValidationUtils.requireTrue(socket.bind(config.getEndpoint()), "failed to bind to " + config.getEndpoint());
		} else {
			ValidationUtils.requireTrue(socket.connect(config.getEndpoint()), "failed to connect to " + config.getEndpoint());
		}
	}

	@Override
	public boolean isHealthy() {
		return true; // ZeroMQ sockets expose no connection-state probe; failures surface on send
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		if (hasEnvelope) {
			var envelopeValue = isEnvelopeTemplated ? TemplateUtils.substitute(envelope, message) : envelope;
			socket.send(envelopeValue.getBytes(java.nio.charset.StandardCharsets.UTF_8), ZMQ.SNDMORE);
		}

		socket.send(message.eventBody());
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(socket, "socket");
		ValidationUtils.tryClose(context, "context");
	}
}