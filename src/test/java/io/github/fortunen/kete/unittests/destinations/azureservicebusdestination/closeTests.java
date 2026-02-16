package io.github.fortunen.kete.unittests.destinations.azureservicebusdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.azure.messaging.servicebus.ServiceBusSenderClient;

import io.github.fortunen.kete.destinations.azureservicebus.AzureServiceBusDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

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
	public void shouldCloseSenderClientAndClearCache() {

		// arrange

		var senderClient = mock(ServiceBusSenderClient.class);
		var cachedClient = mock(ServiceBusSenderClient.class);
		destination.setSenderClient(senderClient);
		destination.getSenderClientCache().put("cached-queue", cachedClient);

		// act

		destination.close();

		// assert

		verify(senderClient).close();
		verify(cachedClient).close();
		assertThat(destination.getSenderClientCache()).isEmpty();
	}

	@Test
	public void shouldHandleNullSenderClient() {

		// arrange

		destination.setSenderClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
