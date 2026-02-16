package io.github.fortunen.kete.unittests.destinations.azureeventgriddestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.azureeventgrid.AzureEventGridDestination;
import io.github.fortunen.kete.destinations.azureeventgrid.AzureEventGridDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static AzureEventGridDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AzureEventGridDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSendEventGridEvent() {

		// arrange

		var publisherClient = (EventGridPublisherClient<EventGridEvent>) mock(EventGridPublisherClient.class);
		destination.setConfig(mock(AzureEventGridDestinationConfig.class));
		destination.setPublisherClient(publisherClient);
		destination.setSubject("test-subject");
		destination.setHasSubject(true);
		destination.setSubjectTemplated(false);
		destination.setEventType("keycloak-event");
		destination.setEventTypeTemplated(false);
		destination.setDataVersion("1.0");

		var body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(EventGridEvent.class);
		verify(publisherClient).sendEvent(captor.capture());
		var event = captor.getValue();
		assertThat(event.getSubject()).isEqualTo("test-subject");
		assertThat(event.getEventType()).isEqualTo("keycloak-event");
		assertThat(event.getDataVersion()).isEqualTo("1.0");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldUseEventTypeAsSubjectWhenNoSubjectConfigured() {

		// arrange

		var publisherClient = (EventGridPublisherClient<EventGridEvent>) mock(EventGridPublisherClient.class);
		destination.setConfig(mock(AzureEventGridDestinationConfig.class));
		destination.setPublisherClient(publisherClient);
		destination.setHasSubject(false);
		destination.setEventType("keycloak-event");
		destination.setEventTypeTemplated(false);
		destination.setDataVersion("1.0");

		var body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(EventGridEvent.class);
		verify(publisherClient).sendEvent(captor.capture());
		assertThat(captor.getValue().getSubject()).isEqualTo("LOGIN");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
