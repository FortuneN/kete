package io.github.fortunen.kete.unittests.destinations.pulsardestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.pulsar.PulsarDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

@SuppressWarnings("unchecked")
public class closeTests {

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
	public void shouldCloseProducerAndClient() throws Exception {

		// arrange

		var producer = mock(Producer.class);
		var client = mock(PulsarClient.class);
		destination.setDefaultProducer(producer);
		destination.setClient(client);

		// act

		destination.close();

		// assert

		verify(producer).close();
		verify(client).close();
	}

	@Test
	public void shouldHandleNullProducerAndClient() {

		// arrange

		destination.setDefaultProducer(null);
		destination.setClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
