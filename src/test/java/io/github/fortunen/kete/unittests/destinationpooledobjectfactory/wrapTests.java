package io.github.fortunen.kete.unittests.destinationpooledobjectfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationPooledObjectFactory;

public class wrapTests {

	@Test
	public void shouldWrapDestinationInPooledObject() {

		// arrange

		var factory = new DestinationPooledObjectFactory();
		var destination = mock(Destination.class);

		// act

		var result = factory.wrap(destination);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(DefaultPooledObject.class);
	}

	@Test
	public void shouldReturnPooledObjectContainingDestination() {

		// arrange

		var factory = new DestinationPooledObjectFactory();
		var destination = mock(Destination.class);

		// act

		var result = factory.wrap(destination);

		// assert

		assertThat(result.getObject()).isSameAs(destination);
	}
}
