package io.github.fortunen.kete.unittests.destinations.azureservicebusdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.azureservicebus.AzureServiceBusDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static AzureServiceBusDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AzureServiceBusDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendMessageToQueue() {

		// arrange

		var senderClient = mock(ServiceBusSenderClient.class);
		destination.setSenderClient(senderClient);
		destination.setQueue("test-queue");
		destination.setHasQueue(true);
		destination.setHasTopic(false);
		destination.setQueueTemplated(false);
		destination.setTopicTemplated(false);
		destination.setHasSubject(false);
		destination.setHasSessionId(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(ServiceBusMessage.class);
		verify(senderClient).sendMessage(captor.capture());
		var sbMessage = captor.getValue();
		assertThat(sbMessage.getBody().toBytes()).isEqualTo(body);
		assertThat(sbMessage.getContentType()).isEqualTo("application/json");
		assertThat(sbMessage.getApplicationProperties()).containsEntry(Constants.MESSAGE_HEADER_EVENT_KIND, Constants.EVENT);
		assertThat(sbMessage.getApplicationProperties()).containsEntry(Constants.MESSAGE_HEADER_EVENT_TYPE, "LOGIN");
		assertThat(sbMessage.getApplicationProperties()).containsEntry(Constants.MESSAGE_HEADER_CONTENT_TYPE, "application/json");
	}

	@Test
	public void shouldSetSubjectAndSessionId() {

		// arrange

		var senderClient = mock(ServiceBusSenderClient.class);
		destination.setSenderClient(senderClient);
		destination.setQueue("test-queue");
		destination.setHasQueue(true);
		destination.setHasTopic(false);
		destination.setQueueTemplated(false);
		destination.setTopicTemplated(false);
		destination.setHasSubject(true);
		destination.setSubject("my-subject");
		destination.setSubjectTemplated(false);
		destination.setHasSessionId(true);
		destination.setSessionId("session-1");
		destination.setSessionIdTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(ServiceBusMessage.class);
		verify(senderClient).sendMessage(captor.capture());
		var sbMessage = captor.getValue();
		assertThat(sbMessage.getSubject()).isEqualTo("my-subject");
		assertThat(sbMessage.getSessionId()).isEqualTo("session-1");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
