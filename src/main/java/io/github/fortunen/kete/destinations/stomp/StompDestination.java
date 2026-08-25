package io.github.fortunen.kete.destinations.stomp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLSocket;

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

	private String destination;
	private boolean isReceiptEnabled;
	private StompConnection connection;
	private boolean isDestinationTemplated;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		connection = new StompConnection();
		destination = config.getDestination();
		isReceiptEnabled = config.isReceiptEnabled();
		isDestinationTemplated = config.isDestinationTemplated();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();

		// verify connection

		var socket = config.getSocketFactory().createSocket(config.getHost(), config.getPort());
		socket.setSoTimeout(config.getReadTimeoutSeconds() * 1000);

		if (socket instanceof SSLSocket sslSocket && config.getTls().isVerifyHostname()) {
			var params = sslSocket.getSSLParameters();
			params.setEndpointIdentificationAlgorithm("HTTPS");
			sslSocket.setSSLParameters(params);
		}

		connection.open(socket);
		connection.connect(config.getConnectHeaders());
	}

	@Override
	public boolean isHealthy() {
		return true; // StompConnection is a raw socket wrapper with no remote-state signal (Socket.isConnected latches true after a remote close); failed sends cull via the pool's invalidate path
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var body = message.eventBody();
		var headers = new HashMap<String, String>();
		var actualDestination = isDestinationTemplated ? TemplateUtils.substitute(destination, message) : destination;

		for (var entry : customHeadersEntrySet) {
			headers.put(entry.getKey(), entry.getValue());
		}

		headers.put("content-type", message.contentType());
		headers.put("content-length", String.valueOf(body.length));
		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());

		if (isReceiptEnabled) {
			headers.put("receipt", message.eventId());
		}

		// the body goes out as raw bytes under content-length so binary and compressed payloads arrive intact
		// (StompConnection.send() would decode them as text); the frame ends with the STOMP NUL byte

		var frame = new StringBuilder("SEND\n").append("destination:").append(actualDestination).append('\n');

		for (var header : headers.entrySet()) {
			frame.append(header.getKey()).append(':').append(header.getValue()).append('\n');
		}

		frame.append('\n');

		connection.sendFrame(frame.toString(), Arrays.copyOf(body, body.length + 1));

		if (isReceiptEnabled) {
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
