package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Configuration;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;

class retryPendingRoutesTests {

	@Test
	@SuppressWarnings("unchecked")
	void shouldActivateRouteOnceItsDestinationComesUp() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());

		var serializer = mock(Serializer.class);
		var route = mock(Route.class);
		when(route.getName()).thenReturn("late-route");
		when(route.getSerializer()).thenReturn(serializer);
		when(route.getSerializerKind()).thenReturn("json");
		when(route.getDestinationKind()).thenReturn("http");
		when(route.getRealmMatchers()).thenReturn(new Matcher[0]);
		when(route.getEventMatchers()).thenReturn(new Matcher[0]);
		when(route.getInFlight()).thenReturn(new AtomicInteger());
		when(route.getDestinationPool()).thenReturn(mock(GenericObjectPool.class));
		doThrow(new RuntimeException("still down")).doNothing().when(route).initializeDestinations();

		factory.getPendingRoutes().add(route);

		// act - first attempt fails

		factory.retryPendingRoutes();

		// assert

		assertThat(factory.getPendingRoutes()).containsExactly(route);
		assertThat(factory.getSerializersWithRoutes()).isNull();
		verify(route).resetDestinations();

		// act - second attempt succeeds

		factory.retryPendingRoutes();

		// assert

		assertThat(factory.getPendingRoutes()).isEmpty();
		assertThat(factory.getSerializersWithRoutes()).hasSize(1);
		assertThat(factory.getSerializersWithRoutes()[0].serializer()).isSameAs(serializer);
		assertThat(factory.getSerializersWithRoutes()[0].routes()).containsExactly(route);

		// cleanup

		factory.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldJoinExistingSerializerGroupWhenRouteComesUp() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());

		var serializer = mock(Serializer.class);
		var existingRoute = mock(Route.class);
		factory.setSerializersWithRoutes(new SerializerRoutes[] { new SerializerRoutes(serializer, List.of(existingRoute)) });

		var route = mock(Route.class);
		when(route.getName()).thenReturn("late-route");
		when(route.getSerializer()).thenReturn(serializer);
		when(route.getRealmMatchers()).thenReturn(new Matcher[0]);
		when(route.getEventMatchers()).thenReturn(new Matcher[0]);
		when(route.getInFlight()).thenReturn(new AtomicInteger());
		when(route.getDestinationPool()).thenReturn(mock(GenericObjectPool.class));

		factory.getPendingRoutes().add(route);

		// act

		factory.retryPendingRoutes();

		// assert

		assertThat(factory.getPendingRoutes()).isEmpty();
		assertThat(factory.getSerializersWithRoutes()).hasSize(1);
		assertThat(factory.getSerializersWithRoutes()[0].routes()).containsExactly(existingRoute, route);

		// cleanup

		factory.close();
	}

	@Test
	void shouldNotTouchSendPathWhileRouteStaysDown() {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("down-route");
		doThrow(new RuntimeException("still down")).when(route).initializeDestinations();
		factory.getPendingRoutes().add(route);

		var untouched = new SerializerRoutes[] { new SerializerRoutes(mock(Serializer.class), List.of(mock(Route.class))) };
		factory.setSerializersWithRoutes(untouched);

		// act

		factory.retryPendingRoutes();

		// assert

		assertThat(factory.getPendingRoutes()).containsExactly(route);
		assertThat(factory.getSerializersWithRoutes()).isSameAs(untouched);

		// cleanup

		factory.close();
	}
}
