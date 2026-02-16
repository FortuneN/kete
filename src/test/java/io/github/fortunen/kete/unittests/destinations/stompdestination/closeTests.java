package io.github.fortunen.kete.unittests.destinations.stompdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.activemq.transport.stomp.StompConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.stomp.StompDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static StompDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new StompDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseConnection() throws Exception {

		// arrange

		var connection = mock(StompConnection.class);
		destination.setConnection(connection);

		// act

		destination.close();

		// assert

		verify(connection).close();
	}

	@Test
	public void shouldHandleNullConnection() {

		// arrange

		destination.setConnection(null);

		// act & assert — should not throw

		destination.close();
	}
}
