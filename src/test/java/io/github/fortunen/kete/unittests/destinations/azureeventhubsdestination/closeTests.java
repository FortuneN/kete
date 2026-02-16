package io.github.fortunen.kete.unittests.destinations.azureeventhubsdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.azure.messaging.eventhubs.EventHubProducerClient;

import io.github.fortunen.kete.destinations.azureeventhubs.AzureEventHubsDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static AzureEventHubsDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AzureEventHubsDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseProducerClient() {

		// arrange

		var producerClient = mock(EventHubProducerClient.class);
		destination.setProducerClient(producerClient);

		// act

		destination.close();

		// assert

		verify(producerClient).close();
	}

	@Test
	public void shouldHandleNullProducerClient() {

		// arrange

		destination.setProducerClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
