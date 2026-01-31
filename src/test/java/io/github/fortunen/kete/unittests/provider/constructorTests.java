package io.github.fortunen.kete.unittests.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import io.github.fortunen.kete.Provider;
import org.junit.jupiter.api.Test;
import org.keycloak.events.EventListenerTransaction;

public class constructorTests {

	@Test
	public void shouldCreateInstanceWithValidTransaction() {

		// arrange

		var transaction = mock(EventListenerTransaction.class);

		// act

		var provider = new Provider(transaction);

		// assert

		assertThat(provider).isNotNull();
		assertThat(provider.getTransaction()).isSameAs(transaction);
	}

	@Test
	public void shouldThrowWhenTransactionIsNull() {

		// arrange

		EventListenerTransaction transaction = null;

		// act

		var thrown = catchThrowable(() -> {
			new Provider(transaction);
		});

		// assert

		assertThat(thrown).isNotNull();
		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("transaction is required");
	}
}
