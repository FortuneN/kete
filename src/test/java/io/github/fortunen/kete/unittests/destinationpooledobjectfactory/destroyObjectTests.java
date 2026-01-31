package io.github.fortunen.kete.unittests.destinationpooledobjectfactory;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationPooledObjectFactory;

public class destroyObjectTests {

	@Test
	public void shouldCloseDestination() throws Exception {

		// arrange

		var factory = new DestinationPooledObjectFactory();
		var destination = mock(Destination.class);
		var pooledObject = new DefaultPooledObject<Destination<?>>(destination);

		// act

		factory.destroyObject(pooledObject);

		// assert

		verify(destination).close();
	}

	@Test
	public void shouldNotThrowWhenCloseThrows() throws Exception {

		// arrange

		var factory = new DestinationPooledObjectFactory();
		var destination = mock(Destination.class);
		doThrow(new RuntimeException("close failed")).when(destination).close();
		var pooledObject = new DefaultPooledObject<Destination<?>>(destination);

		// act & assert

		assertThatCode(() -> factory.destroyObject(pooledObject)).doesNotThrowAnyException();
	}
}
