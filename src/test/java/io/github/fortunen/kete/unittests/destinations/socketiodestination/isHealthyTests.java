package io.github.fortunen.kete.unittests.destinations.socketiodestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.socketio.SocketIODestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.socket.client.Socket;

public class isHealthyTests {

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
	public void shouldReturnTrueWhenSocketIsConnected() {

		// arrange

		var socket = mock(Socket.class);
		when(socket.connected()).thenReturn(true);
		destination.setSocket(socket);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenSocketIsNotConnected() {

		// arrange

		var socket = mock(Socket.class);
		when(socket.connected()).thenReturn(false);
		destination.setSocket(socket);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSocketIsNull() {

		// arrange

		destination.setSocket(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}
}
