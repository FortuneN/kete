package io.github.fortunen.kete.unittests.destinations.amqp1destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import jakarta.jms.BytesMessage;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.amqp1.Amqp1Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

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
	public void shouldSendToQueueDestination() throws Exception {

		// arrange

		var session = mock(Session.class);
		var producer = mock(MessageProducer.class);
		var jmsDestination = mock(Queue.class);
		var bytesMessage = mock(BytesMessage.class);
		when(session.createBytesMessage()).thenReturn(bytesMessage);

		destination.setSession(session);
		destination.setProducer(producer);
		destination.setJmsDestination(jmsDestination);
		destination.setQueueOrTopicName("test-queue");
		destination.setDestinationIsQueue(true);
		destination.setQueueOrTopicNameTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(session).createBytesMessage();
		verify(bytesMessage).writeBytes(body);
		verify(producer).send(eq(jmsDestination), eq(bytesMessage));
	}

	@Test
	public void shouldSetStandardHeaders() throws Exception {

		// arrange

		var session = mock(Session.class);
		var producer = mock(MessageProducer.class);
		var jmsDestination = mock(Queue.class);
		var bytesMessage = mock(BytesMessage.class);
		when(session.createBytesMessage()).thenReturn(bytesMessage);

		destination.setSession(session);
		destination.setProducer(producer);
		destination.setJmsDestination(jmsDestination);
		destination.setQueueOrTopicName("test-queue");
		destination.setDestinationIsQueue(true);
		destination.setQueueOrTopicNameTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(bytesMessage).setStringProperty(Constants.MESSAGE_HEADER_EVENT_KIND, Constants.EVENT);
		verify(bytesMessage).setStringProperty(Constants.MESSAGE_HEADER_EVENT_TYPE, "LOGIN");
		verify(bytesMessage).setStringProperty(Constants.MESSAGE_HEADER_CONTENT_TYPE, "application/json");
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
