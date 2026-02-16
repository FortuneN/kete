package io.github.fortunen.kete.unittests.destinations.azureeventhubsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.SendOptions;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.azureeventhubs.AzureEventHubsDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static AzureEventHubsDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AzureEventHubsDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSendEventDataWithHeaders() {

		// arrange

		var producerClient = mock(EventHubProducerClient.class);
		destination.setProducerClient(producerClient);
		destination.setHasPartitionKey(false);
		destination.setHasPartitionId(false);
		destination.setPartitionKeyTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var listCaptor = ArgumentCaptor.forClass(List.class);
		var optionsCaptor = ArgumentCaptor.forClass(SendOptions.class);
		verify(producerClient).send(listCaptor.capture(), optionsCaptor.capture());
		var events = (List<EventData>) listCaptor.getValue();
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getBodyAsString()).isEqualTo("test-body");
		assertThat(events.get(0).getProperties()).containsEntry(Constants.MESSAGE_HEADER_EVENT_KIND, Constants.EVENT);
		assertThat(events.get(0).getProperties()).containsEntry(Constants.MESSAGE_HEADER_EVENT_TYPE, "LOGIN");
		assertThat(events.get(0).getProperties()).containsEntry(Constants.MESSAGE_HEADER_CONTENT_TYPE, "application/json");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSetPartitionKey() {

		// arrange

		var producerClient = mock(EventHubProducerClient.class);
		destination.setProducerClient(producerClient);
		destination.setHasPartitionKey(true);
		destination.setPartitionKey("pk-1");
		destination.setPartitionKeyTemplated(false);
		destination.setHasPartitionId(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var optionsCaptor = ArgumentCaptor.forClass(SendOptions.class);
		verify(producerClient).send(any(List.class), optionsCaptor.capture());
		assertThat(optionsCaptor.getValue().getPartitionKey()).isEqualTo("pk-1");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
