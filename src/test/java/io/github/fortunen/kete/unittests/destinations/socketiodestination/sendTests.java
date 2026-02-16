package io.github.fortunen.kete.unittests.destinations.socketiodestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.socketio.SocketIODestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.socket.client.Socket;

public class sendTests {

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
	public void shouldEmitJsonPayload() {

		// arrange

		var socket = mock(Socket.class);
		destination.setSocket(socket);
		destination.setEventName("keycloak-event");
		destination.setEventNameTemplated(false);

		var body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(Object[].class);
		verify(socket).emit(eq("keycloak-event"), captor.capture());
		var emitted = captor.getValue();
		assertThat(emitted).hasSize(1);
		assertThat(emitted[0]).isInstanceOf(JSONObject.class);
	}

	@Test
	public void shouldEmitTextPayload() {

		// arrange

		var socket = mock(Socket.class);
		destination.setSocket(socket);
		destination.setEventName("keycloak-event");
		destination.setEventNameTemplated(false);

		var body = "plain text".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "text/plain", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(socket).emit(eq("keycloak-event"), eq("plain text"));
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
