package io.github.fortunen.kete.unittests.route;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.Route;

public class closeTests {

	@Test
	@SuppressWarnings("unchecked")
	public void shouldCloseDestinationPool() {

		// arrange

		var route = new Route();
		var destinationPool = mock(GenericObjectPool.class);
		route.setDestinationPool(destinationPool);

		// act

		route.close();

		// assert

		verify(destinationPool).close();
	}

	@Test
	public void shouldNotThrowWhenDestinationPoolIsNull() {

		// arrange

		var route = new Route();

		// act & assert

		assertThatCode(() -> route.close()).doesNotThrowAnyException();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldNotThrowWhenDestinationPoolCloseThrows() {

		// arrange

		var route = new Route();
		route.setName("test-route");
		var destinationPool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		doThrow(new RuntimeException("close failed")).when(destinationPool).close();
		route.setDestinationPool(destinationPool);

		// act & assert

		assertThatCode(() -> route.close()).doesNotThrowAnyException();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldLogWarningWhenDestinationPoolCloseThrows() {

		// arrange

		var route = new Route();
		route.setName("test-route");
		var destinationPool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		doThrow(new RuntimeException("close failed")).when(destinationPool).close();
		route.setDestinationPool(destinationPool);

		// act

		route.close();

		// assert

		verify(destinationPool).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldNotCloseDestinationPoolWhenAlreadyClosed() {

		// arrange

		var route = new Route();
		var destinationPool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		route.setDestinationPool(destinationPool);

		// act

		route.close();
		route.setDestinationPool(null);
		route.close();

		// assert

		verify(destinationPool).close();
	}
}
