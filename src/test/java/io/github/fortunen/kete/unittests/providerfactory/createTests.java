package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.fortunen.kete.Provider;
import io.github.fortunen.kete.ProviderFactory;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;

class createTests {

	@Test
	void shouldThrowWhenSessionIsNull() {

		// arrange

		var factory = new ProviderFactory();

		// act

		var thrown = catchThrowable(() -> factory.create(null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("session is required");
	}

	@Test
	void shouldThrowWhenTransactionManagerIsNull() {

		// arrange

		var factory = new ProviderFactory();
		var session = mock(KeycloakSession.class);
		when(session.getTransactionManager()).thenReturn(null);

		// act

		var thrown = catchThrowable(() -> factory.create(session));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("transactionManager is required");
	}

	@Test
	void shouldReturnProviderInstance() {

		// arrange

		var factory = new ProviderFactory();
		var session = mock(KeycloakSession.class);
		var transactionManager = mock(KeycloakTransactionManager.class);
		when(session.getTransactionManager()).thenReturn(transactionManager);

		// act

		var result = factory.create(session);

		// assert

		assertThat(result).isInstanceOf(Provider.class);
	}

	@Test
	void shouldEnlistTransactionAfterCompletion() {

		// arrange

		var factory = new ProviderFactory();
		var session = mock(KeycloakSession.class);
		var transactionManager = mock(KeycloakTransactionManager.class);
		when(session.getTransactionManager()).thenReturn(transactionManager);

		// act

		factory.create(session);

		// assert

		verify(transactionManager).enlistAfterCompletion(org.mockito.ArgumentMatchers.any());
	}
}
