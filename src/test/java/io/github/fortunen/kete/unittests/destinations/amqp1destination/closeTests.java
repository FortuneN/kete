package io.github.fortunen.kete.unittests.destinations.amqp1destination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.jms.Connection;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.amqp1.Amqp1Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static Amqp1Destination destination;

	@BeforeAll
	static void setUp() {
		destination = new Amqp1Destination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseProducerSessionAndConnection() throws Exception {

		// arrange

		var producer = mock(MessageProducer.class);
		var session = mock(Session.class);
		var connection = mock(Connection.class);
		destination.setProducer(producer);
		destination.setSession(session);
		destination.setConnection(connection);

		// act

		destination.close();

		// assert

		verify(producer).close();
		verify(session).close();
		verify(connection).close();
	}

	@Test
	public void shouldHandleNullResources() {

		// arrange

		destination.setProducer(null);
		destination.setSession(null);
		destination.setConnection(null);

		// act & assert — should not throw

		destination.close();
	}
}
