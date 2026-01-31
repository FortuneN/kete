package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.fortunen.kete.ProviderFactory;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSessionFactory;

class postInitTests {

	@Test
	void shouldThrowWhenSessionFactoryIsNull() {

		// arrange

		var factory = new ProviderFactory();

		// act

		var thrown = catchThrowable(() -> factory.postInit(null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("sessionFactory is required");
	}

	@Test
	void shouldStoreSessionFactory() {

		// arrange

		var factory = new ProviderFactory();
		var sessionFactory = mock(KeycloakSessionFactory.class);

		// act

		factory.postInit(sessionFactory);

		// assert

	assertThat(factory.getPostInitSessionFactory()).isEqualTo(sessionFactory);
}

@Test
void shouldRegisterAsListener() {

	// arrange

	var factory = new ProviderFactory();
	var sessionFactory = mock(KeycloakSessionFactory.class);

	// act

	factory.postInit(sessionFactory);

	// assert

	verify(sessionFactory).register(factory);
}
}
