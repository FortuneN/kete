package io.github.fortunen.kete.unittests.destinations.natsdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.nats.NatsDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;

public class closeTests {

	private static NatsDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new NatsDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseConnection() throws Exception {

		// arrange

		var connection = mock(Connection.class);
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
