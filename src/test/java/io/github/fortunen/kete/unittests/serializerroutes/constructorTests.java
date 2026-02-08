package io.github.fortunen.kete.unittests.serializerroutes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;

class constructorTests {

	@Test
	void shouldCreateWithValidArguments() {

		// arrange

		var serializer = Mockito.mock(Serializer.class);
		var routes = List.of(Mockito.mock(Route.class));

		// act

		var result = new SerializerRoutes(serializer, routes);

		// assert

		assertThat(result.serializer()).isSameAs(serializer);
		assertThat(result.routes()).isSameAs(routes);
	}

	@Test
	void shouldThrowForNullSerializer() {

		// act

		var thrown = catchThrowable(() -> new SerializerRoutes(null, List.of()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("serializer is required");
	}

	@Test
	void shouldThrowForNullRoutes() {

		// arrange

		var serializer = Mockito.mock(Serializer.class);

		// act

		var thrown = catchThrowable(() -> new SerializerRoutes(serializer, null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("routes is required");
	}
}
