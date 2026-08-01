package io.github.fortunen.kete.unittests.destinations.pulsardestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.pulsar.client.api.Producer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.pulsar.PulsarDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

@SuppressWarnings("unchecked")
public class isHealthyTests {

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
	public void shouldReturnTrueWhenProducerIsConnected() {

		// arrange

		var producer = mock(Producer.class);
		when(producer.isConnected()).thenReturn(true);
		destination.setTopicTemplated(false);
		destination.setDefaultProducer(producer);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenProducerIsNotConnected() {

		// arrange

		var producer = mock(Producer.class);
		when(producer.isConnected()).thenReturn(false);
		destination.setTopicTemplated(false);
		destination.setDefaultProducer(producer);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenProducerIsNull() {

		// arrange

		destination.setTopicTemplated(false);
		destination.setDefaultProducer(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenTopicIsTemplated() {

		// arrange

		destination.setTopicTemplated(true);
		destination.setDefaultProducer(null);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}
}
