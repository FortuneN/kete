package io.github.fortunen.kete.unittests.destinations.zeromqdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import io.github.fortunen.kete.destinations.zeromq.ZeroMQDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

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
	public void shouldCloseSocketAndContext() {

		// arrange

		var socket = mock(ZMQ.Socket.class);
		var context = mock(ZContext.class);
		destination.setSocket(socket);
		destination.setContext(context);

		// act

		destination.close();

		// assert

		verify(socket).close();
		verify(context).close();
	}

	@Test
	public void shouldHandleNullSocketAndContext() {

		// arrange

		destination.setSocket(null);
		destination.setContext(null);

		// act & assert — should not throw

		destination.close();
	}
}
