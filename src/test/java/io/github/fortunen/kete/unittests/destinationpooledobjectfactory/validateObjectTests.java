package io.github.fortunen.kete.unittests.destinationpooledobjectfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationPooledObjectFactory;

public class validateObjectTests {

	@Test
	public void shouldReturnTrueWhenDestinationIsHealthy() {

		// arrange

		var destination = mock(Destination.class);
		when(destination.isHealthy()).thenReturn(true);

		var factory = new DestinationPooledObjectFactory();
		var pooledObject = factory.wrap(destination);

		// act

		var result = factory.validateObject(pooledObject);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenDestinationIsUnhealthy() {

		// arrange

		var destination = mock(Destination.class);
		when(destination.isHealthy()).thenReturn(false);

		var factory = new DestinationPooledObjectFactory();
		var pooledObject = factory.wrap(destination);

		// act

		var result = factory.validateObject(pooledObject);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenIsHealthyThrows() {

		// arrange

		var destination = mock(Destination.class);
		when(destination.isHealthy()).thenThrow(new RuntimeException("probe failed"));

		var factory = new DestinationPooledObjectFactory();
		var pooledObject = factory.wrap(destination);

		// act

		var result = factory.validateObject(pooledObject);

		// assert

		assertThat(result).isFalse();
	}
}
