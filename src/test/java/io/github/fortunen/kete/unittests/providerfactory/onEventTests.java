package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import io.github.fortunen.kete.ProviderFactory;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderEvent;

class onEventTests {

	@Test
	void shouldThrowWhenProviderEventIsNull() {

		// arrange

		var factory = new ProviderFactory();
		var sessionFactory = mock(KeycloakSessionFactory.class);
		factory.postInit(sessionFactory);

		// act

		var thrown = catchThrowable(() -> factory.onEvent(null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("providerEvent is required");
	}

	@Test
	void shouldThrowWhenPostInitSessionFactoryIsNull() {

		// arrange

		var factory = new ProviderFactory();
		var event = mock(ProviderEvent.class);

		// act

		var thrown = catchThrowable(() -> factory.onEvent(event));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("postInitSessionFactory is required");
	}

	@Test
	void shouldNotThrowWhenNonPostMigrationEvent() {

		// arrange

		var factory = new ProviderFactory();
		var sessionFactory = mock(KeycloakSessionFactory.class);
		factory.postInit(sessionFactory);
		var event = mock(ProviderEvent.class);

		// act

		var thrown = catchThrowable(() -> factory.onEvent(event));

		// assert

		assertThat(thrown).isNull();
	}
}
