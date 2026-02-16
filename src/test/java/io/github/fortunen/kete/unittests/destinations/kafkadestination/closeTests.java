package io.github.fortunen.kete.unittests.destinations.kafkadestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.kafka.KafkaDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

@SuppressWarnings("unchecked")
public class closeTests {

	private static KafkaDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new KafkaDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseProducer() {

		// arrange

		var producer = mock(KafkaProducer.class);
		destination.setProducer(producer);

		// act

		destination.close();

		// assert

		verify(producer).close();
	}

	@Test
	public void shouldHandleNullProducer() {

		// arrange

		destination.setProducer(null);

		// act & assert — should not throw

		destination.close();
	}
}
