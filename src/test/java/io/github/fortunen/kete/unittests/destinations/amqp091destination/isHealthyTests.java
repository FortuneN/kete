package io.github.fortunen.kete.unittests.destinations.amqp091destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.github.fortunen.kete.destinations.amqp091.Amqp091Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class isHealthyTests {

	private static Amqp091Destination destination;

	@BeforeAll
	static void setUp() {
		destination = new Amqp091Destination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldReturnTrueWhenConnectionAndChannelAreOpen() {

		// arrange

		var connection = mock(Connection.class);
		var channel = mock(Channel.class);
		when(connection.isOpen()).thenReturn(true);
		when(channel.isOpen()).thenReturn(true);
		destination.setConnection(connection);
		destination.setChannel(channel);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsNotOpen() {

		// arrange

		var connection = mock(Connection.class);
		var channel = mock(Channel.class);
		when(connection.isOpen()).thenReturn(false);
		when(channel.isOpen()).thenReturn(true);
		destination.setConnection(connection);
		destination.setChannel(channel);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenChannelIsNotOpen() {

		// arrange

		var connection = mock(Connection.class);
		var channel = mock(Channel.class);
		when(connection.isOpen()).thenReturn(true);
		when(channel.isOpen()).thenReturn(false);
		destination.setConnection(connection);
		destination.setChannel(channel);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsNull() {

		// arrange

		destination.setConnection(null);
		destination.setChannel(mock(Channel.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}
}
