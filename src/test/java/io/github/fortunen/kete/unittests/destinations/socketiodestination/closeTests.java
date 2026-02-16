package io.github.fortunen.kete.unittests.destinations.socketiodestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.socketio.SocketIODestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.socket.client.Socket;

public class closeTests {

	private static SocketIODestination destination;

	@BeforeAll
	static void setUp() {
		destination = new SocketIODestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseSocket() {

		// arrange

		var socket = mock(Socket.class);
		destination.setSocket(socket);

		// act

		destination.close();

		// assert

		verify(socket).close();
	}

	@Test
	public void shouldHandleNullSocket() {

		// arrange

		destination.setSocket(null);

		// act & assert — should not throw

		destination.close();
	}
}
