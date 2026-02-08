package io.github.fortunen.kete.destinations.zeromq;

import org.zeromq.SocketType;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class ZeroMQDestinationConfig extends DestinationConfig {

	public static final String PUSH = "PUSH";
	public static final String BIND = "BIND";
	public static final String LINGER = "linger";
	public static final int DEFAULT_LINGER = 1000;
	public static final String CONNECT = "CONNECT";
	public static final String PUBLISH = "PUBLISH";
	public static final String ENDPOINT = "endpoint";
	public static final String SOCKET_TYPE = "socket-type";
	public static final String DEFAULT_SOCKET_TYPE = PUBLISH;
	public static final String DEFAULT_CONNECTION_MODE = CONNECT;
	public static final String CONNECTION_MODE = "connection-mode";

	private int linger;
	private String endpoint;
	private String socketType;
	private String connectionMode;
	private SocketType socketTypeValue;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		// endpoint

		endpoint = ValidationUtils.requireNonBlank(configuration.getString(ENDPOINT, "").trim(), ENDPOINT + " is required");

		// socket type

		socketType = configuration.getString(SOCKET_TYPE, DEFAULT_SOCKET_TYPE).trim().toUpperCase();

		ValidationUtils.requireTrue(socketType.equals(PUBLISH) || socketType.equals(PUSH), SOCKET_TYPE + " must be one of : " + PUBLISH + " or " + PUSH);

		if (socketType.equals(PUBLISH)) {
			socketTypeValue = SocketType.PUB;
		} else {
			socketTypeValue = SocketType.PUSH;
		}

		// connection mode

		connectionMode = configuration.getString(CONNECTION_MODE, DEFAULT_CONNECTION_MODE).trim().toUpperCase();

		ValidationUtils.requireTrue(connectionMode.equals(BIND) || connectionMode.equals(CONNECT), CONNECTION_MODE + " must be one of : " + BIND + " or " + CONNECT);

		// linger (milliseconds to wait for pending messages on close, -1 = infinite, 0 = discard immediately)

		linger = configuration.getInt(LINGER, DEFAULT_LINGER);

		ValidationUtils.requireGreaterThan(linger, -2, LINGER + " must be -1 or greater");
	}
}