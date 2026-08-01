package io.github.fortunen.kete.unittests.destinations.natsjetstreamdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.natsjetstream.NatsJetStreamDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;
import io.nats.client.JetStream;

public class isHealthyTests {

	private static NatsJetStreamDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new NatsJetStreamDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldReturnTrueWhenConnectionIsConnectedAndJetStreamIsSet() {

		// arrange

		var connection = mock(Connection.class);
		when(connection.getStatus()).thenReturn(Connection.Status.CONNECTED);
		destination.setConnection(connection);
		destination.setJetStream(mock(JetStream.class));

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsClosed() {

		// arrange

		var connection = mock(Connection.class);
		when(connection.getStatus()).thenReturn(Connection.Status.CLOSED);
		destination.setConnection(connection);
		destination.setJetStream(mock(JetStream.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenJetStreamIsNull() {

		// arrange

		var connection = mock(Connection.class);
		when(connection.getStatus()).thenReturn(Connection.Status.CONNECTED);
		destination.setConnection(connection);
		destination.setJetStream(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsNull() {

		// arrange

		destination.setConnection(null);
		destination.setJetStream(mock(JetStream.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}
}
