package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.SerializerRoutes;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;

class acceptEventTests {

	@Test
	void shouldGracefullyIgnoreNullEvent() {

		// arrange

		var factory = new ProviderFactory();

		// act & assert

		assertThatCode(() -> factory.accept((Event) null)).doesNotThrowAnyException();
	}

	@Test
	void shouldGracefullyHandleUninitializedState() {

		// arrange

		var factory = new ProviderFactory();
		var event = mock(Event.class);

		// act & assert

		assertThatCode(() -> factory.accept(event)).doesNotThrowAnyException();
	}

	@Test
	void shouldGracefullyHandleEmptyRoutes() {

		// arrange

		var factory = new ProviderFactory();
		factory.setSerializersWithRoutes(new SerializerRoutes[0]);
		var event = mock(Event.class);

		// act & assert

		assertThatCode(() -> factory.accept(event)).doesNotThrowAnyException();
	}
}
