package io.github.fortunen.kete.unittests.destinations.natsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.nats.NatsDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;

public class isHealthyTests {

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
	public void shouldReturnTrueWhenConnectionIsConnected() {

		// arrange

		var connection = mock(Connection.class);
		when(connection.getStatus()).thenReturn(Connection.Status.CONNECTED);
		destination.setConnection(connection);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsClosed() {

		// arrange

		var connection = mock(Connection.class);
		when(connection.getStatus()).thenReturn(Connection.Status.CLOSED);
		destination.setConnection(connection);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsNull() {

		// arrange

		destination.setConnection(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}
}
