package io.github.fortunen.kete.unittests.destinations.amqp1destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.qpid.jms.JmsConnection;
import org.apache.qpid.jms.JmsSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.amqp1.Amqp1Destination;
import io.github.fortunen.kete.utils.ValidationUtils;
import jakarta.jms.Connection;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;

public class isHealthyTests {

	private static Amqp1Destination destination;

	@BeforeAll
	static void setUp() {
		destination = new Amqp1Destination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	private static JmsConnection mockConnectedJmsConnection() {
		var jmsConnection = mock(JmsConnection.class);
		when(jmsConnection.isConnected()).thenReturn(true);
		return jmsConnection;
	}

	@Test
	public void shouldReturnTrueWhenConnectionIsConnected() {

		// arrange

		destination.setConnection(mockConnectedJmsConnection());
		destination.setSession(mock(JmsSession.class));
		destination.setProducer(mock(MessageProducer.class));

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsFailed() {

		// arrange

		var jmsConnection = mockConnectedJmsConnection();
		when(jmsConnection.isFailed()).thenReturn(true);
		destination.setConnection(jmsConnection);
		destination.setSession(mock(JmsSession.class));
		destination.setProducer(mock(MessageProducer.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsClosed() {

		// arrange

		var jmsConnection = mockConnectedJmsConnection();
		when(jmsConnection.isClosed()).thenReturn(true);
		destination.setConnection(jmsConnection);
		destination.setSession(mock(JmsSession.class));
		destination.setProducer(mock(MessageProducer.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsNotConnected() {

		// arrange

		var jmsConnection = mock(JmsConnection.class);
		when(jmsConnection.isConnected()).thenReturn(false);
		destination.setConnection(jmsConnection);
		destination.setSession(mock(JmsSession.class));
		destination.setProducer(mock(MessageProducer.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSessionIsClosed() {

		// arrange

		var jmsSession = mock(JmsSession.class);
		when(jmsSession.isClosed()).thenReturn(true);
		destination.setConnection(mockConnectedJmsConnection());
		destination.setSession(jmsSession);
		destination.setProducer(mock(MessageProducer.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenConnectionIsNull() {

		// arrange

		destination.setConnection(null);
		destination.setSession(mock(JmsSession.class));
		destination.setProducer(mock(MessageProducer.class));

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnTrueForNonQpidConnection() {

		// arrange (portability: a non-Qpid JMS provider exposes no state, so the probe passes)

		destination.setConnection(mock(Connection.class));
		destination.setSession(mock(Session.class));
		destination.setProducer(mock(MessageProducer.class));

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}
}
