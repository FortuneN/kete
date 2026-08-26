package io.github.fortunen.kete.unittests.route;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.Route;

public class resetDestinationsTests {

	@Test
	@SuppressWarnings("unchecked")
	public void shouldCloseAndDropThePool() {

		// arrange

		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		var route = new Route();
		route.setName("test-route");
		route.setDestinationPool(pool);

		// act

		route.resetDestinations();

		// assert

		verify(pool).close();
		assertThat(route.getDestinationPool()).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldDropThePoolEvenWhenCloseFails() {

		// arrange

		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		doThrow(new RuntimeException("close failed")).when(pool).close();
		var route = new Route();
		route.setName("test-route");
		route.setDestinationPool(pool);

		// act & assert

		assertThatCode(route::resetDestinations).doesNotThrowAnyException();
		assertThat(route.getDestinationPool()).isNull();
	}

	@Test
	public void shouldDoNothingWithoutAPool() {

		// arrange

		var route = new Route();

		// act & assert

		assertThatCode(route::resetDestinations).doesNotThrowAnyException();
		assertThat(route.getDestinationPool()).isNull();
	}
}
