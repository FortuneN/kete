package io.github.fortunen.kete.unittests.destinations.amqp091destination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.amqp091.Amqp091Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

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
	public void shouldCloseChannelAndConnection() throws Exception {

		// arrange

		var channel = mock(Channel.class);
		var connection = mock(Connection.class);
		destination.setChannel(channel);
		destination.setConnection(connection);

		// act

		destination.close();

		// assert

		verify(channel).close();
		verify(connection).close();
	}

	@Test
	public void shouldHandleNullChannelAndConnection() {

		// arrange

		destination.setChannel(null);
		destination.setConnection(null);

		// act & assert — should not throw

		destination.close();
	}
}
