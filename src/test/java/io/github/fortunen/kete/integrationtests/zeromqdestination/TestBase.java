package io.github.fortunen.kete.integrationtests.zeromqdestination;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.zeromq.ZeroMQDestination;
import io.github.fortunen.kete.destinations.zeromq.ZeroMQDestinationConfig;

@SuppressWarnings("resource")
public class TestBase {

	protected static final byte[] EMPTY_BYTES = new byte[0];

	protected ZeroMQDestination destination;
	protected ZeroMQDestinationConfig config;
	protected ZContext consumerContext;

	@BeforeEach
	void setUp() {
		destination = new ZeroMQDestination();
		config = new ZeroMQDestinationConfig();
		consumerContext = new ZContext(1);
	}

	protected void configureDestination(MapConfiguration mapConfig) {
		mapConfig.setProperty(Constants.KIND, "zeromq");
		config.setConfiguration(mapConfig);
		config.initialize();
		destination.setConfig(config);
	}

	protected int findAvailablePort() throws IOException {
		try (var serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
	}

	protected ZMQ.Socket createSubscriber(int port, String endpoint) {

		var socket = consumerContext.createSocket(SocketType.SUB);
		socket.setReceiveTimeOut(10_000);
		socket.subscribe(ZMQ.SUBSCRIPTION_ALL);
		socket.connect(endpoint != null ? endpoint : ("tcp://localhost:" + port));

		return socket;
	}

	protected ZMQ.Socket createPullConsumer(int port, String endpoint) {

		var socket = consumerContext.createSocket(SocketType.PULL);
		socket.setReceiveTimeOut(10_000);
		socket.connect(endpoint != null ? endpoint : ("tcp://localhost:" + port));

		return socket;
	}

	protected ZMQ.Socket createPullConsumerThatBinds(String endpoint) {

		var socket = consumerContext.createSocket(SocketType.PULL);
		socket.setReceiveTimeOut(10_000);
		socket.bind(endpoint);

		return socket;
	}

	@AfterEach
	protected void cleanUp() {

		if (destination != null) {
			try {
				destination.close();
			} catch (Exception exception) {
				// ignore
			}
		}

		destination = null;

		if (consumerContext != null) {
			try {
				consumerContext.close();
			} catch (Exception exception) {
				// ignore
			}
		}

		consumerContext = null;
	}

	protected static EventMessage createMessage(
		String eventId,
		String realm,
		boolean isAdminEvent,
		String eventType,
		String contentType,
		byte[] eventBody,
		String resourceType,
		String operationType
	) {
		return new EventMessage(
			realm != null ? realm : "",
			eventId != null ? eventId : "",
			eventBody != null ? eventBody : EMPTY_BYTES,
			eventType != null ? eventType : "",
			contentType != null ? contentType : "",
			resourceType != null ? resourceType : "",
			isAdminEvent ? Constants.ADMIN_EVENT : Constants.EVENT,
			operationType != null ? operationType : "",
			"SUCCESS"
		);
	}
}
