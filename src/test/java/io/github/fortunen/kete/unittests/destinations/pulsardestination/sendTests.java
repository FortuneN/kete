package io.github.fortunen.kete.unittests.destinations.pulsardestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.pulsar.PulsarDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

@SuppressWarnings("unchecked")
public class sendTests {

	private static PulsarDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new PulsarDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendMessage() throws Exception {

		// arrange

		var producer = mock(Producer.class);
		var messageBuilder = mock(TypedMessageBuilder.class);
		when(producer.newMessage()).thenReturn(messageBuilder);
		when(messageBuilder.value(any())).thenReturn(messageBuilder);
		when(messageBuilder.key(any())).thenReturn(messageBuilder);
		when(messageBuilder.property(any(), any())).thenReturn(messageBuilder);

		destination.setDefaultProducer(producer);
		destination.setTopic("test-topic");
		destination.setTopicTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(producer).newMessage();
		verify(messageBuilder).value(body);
		verify(messageBuilder).key("LOGIN");
		verify(messageBuilder).send();
	}

	@Test
	public void shouldSetStandardHeaders() throws Exception {

		// arrange

		var producer = mock(Producer.class);
		var messageBuilder = mock(TypedMessageBuilder.class);
		when(producer.newMessage()).thenReturn(messageBuilder);
		when(messageBuilder.value(any())).thenReturn(messageBuilder);
		when(messageBuilder.key(any())).thenReturn(messageBuilder);
		when(messageBuilder.property(any(), any())).thenReturn(messageBuilder);

		destination.setDefaultProducer(producer);
		destination.setTopic("test-topic");
		destination.setTopicTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(messageBuilder).property(Constants.MESSAGE_HEADER_EVENT_KIND, Constants.EVENT);
		verify(messageBuilder).property(Constants.MESSAGE_HEADER_EVENT_TYPE, "LOGIN");
		verify(messageBuilder).property(Constants.MESSAGE_HEADER_CONTENT_TYPE, "application/json");
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
