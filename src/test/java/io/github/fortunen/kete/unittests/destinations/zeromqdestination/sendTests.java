package io.github.fortunen.kete.unittests.destinations.zeromqdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zeromq.ZMQ;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.zeromq.ZeroMQDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static ZeroMQDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new ZeroMQDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendBody() {

		// arrange

		var socket = mock(ZMQ.Socket.class);
		when(socket.send(any(byte[].class))).thenReturn(true);
		when(socket.send(any(byte[].class), anyInt())).thenReturn(true);
		destination.setSocket(socket);
		destination.setHasEnvelope(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(socket).send(eq(body));
	}

	@Test
	public void shouldThrowWhenSendFails() {

		// arrange

		var socket = mock(ZMQ.Socket.class);
		when(socket.send(any(byte[].class))).thenReturn(false);
		destination.setSocket(socket);
		destination.setHasEnvelope(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		var thrown = catchThrowable(() -> destination.doSend(message));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("failed to send message");
	}

	@Test
	public void shouldSendWithEnvelope() {

		// arrange

		var socket = mock(ZMQ.Socket.class);
		when(socket.send(any(byte[].class))).thenReturn(true);
		when(socket.send(any(byte[].class), anyInt())).thenReturn(true);
		destination.setSocket(socket);
		destination.setHasEnvelope(true);
		destination.setEnvelope("test-envelope");
		destination.setEnvelopeTemplated(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(socket).send(eq("test-envelope".getBytes(StandardCharsets.UTF_8)), eq(ZMQ.SNDMORE));
		verify(socket).send(eq(body));
	}

	@Test
	public void shouldSendWithTemplatedEnvelope() {

		// arrange

		var socket = mock(ZMQ.Socket.class);
		when(socket.send(any(byte[].class))).thenReturn(true);
		when(socket.send(any(byte[].class), anyInt())).thenReturn(true);
		destination.setSocket(socket);
		destination.setHasEnvelope(true);
		destination.setEnvelope("events-${realmLowerCase}");
		destination.setEnvelopeTemplated(true);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(socket).send(eq("events-my-realm".getBytes(StandardCharsets.UTF_8)), eq(ZMQ.SNDMORE));
		verify(socket).send(eq(body));
	}

	@Test
	public void shouldNotSendEnvelopeWhenNotConfigured() {

		// arrange

		var socket = mock(ZMQ.Socket.class);
		when(socket.send(any(byte[].class))).thenReturn(true);
		when(socket.send(any(byte[].class), anyInt())).thenReturn(true);
		destination.setSocket(socket);
		destination.setHasEnvelope(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(socket, never()).send(any(byte[].class), eq(ZMQ.SNDMORE));
		verify(socket).send(eq(body));
	}

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}
}
