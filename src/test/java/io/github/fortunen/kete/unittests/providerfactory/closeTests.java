package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class closeTests {

	@Test
	void shouldNotThrowWhenExecutorIsNull() {

		// arrange

		var factory = new ProviderFactory();

		// act & assert

		assertThatCode(() -> factory.close()).doesNotThrowAnyException();
	}

	@Test
	void shouldNotThrowWhenRoutesBySerializerIsNull() {

		// arrange

		var factory = new ProviderFactory();
		factory.setEventExecutor(mock(ExecutorService.class));

		// act & assert

		assertThatCode(() -> factory.close()).doesNotThrowAnyException();
	}

	@Test
	void shouldCloseExecutor() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		var executor = mock(ExecutorService.class);
		when(executor.awaitTermination(30L, TimeUnit.SECONDS)).thenReturn(true);
		factory.setEventExecutor(executor);

		// act

		factory.close();

		// assert

		verify(executor).shutdown();
		verify(executor).awaitTermination(30L, TimeUnit.SECONDS);
	}

	@Test
	void shouldCloseRoutes() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		var executor = mock(ExecutorService.class);
		when(executor.isShutdown()).thenReturn(true);
		factory.setEventExecutor(executor);

		var route = mock(Route.class);

		var serializer = mock(Serializer.class);
		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		// act

		factory.close();

		// assert

		verify(route).close();
	}

	@Test
	void shouldNotThrowWhenRouteCloseThrows() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		var executor = mock(ExecutorService.class);
		when(executor.isShutdown()).thenReturn(true);
		factory.setEventExecutor(executor);

		var route = mock(Route.class);
		doThrow(new RuntimeException("close failed")).when(route).close();
		when(route.getName()).thenReturn("test-route");

		var serializer = mock(Serializer.class);
		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		// act & assert

		assertThatCode(() -> factory.close()).doesNotThrowAnyException();
	}
}
