package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import io.github.fortunen.kete.ProviderFactory;
import org.junit.jupiter.api.Test;
import org.keycloak.Config.Scope;

class initTests {

	@Test
	void shouldThrowWhenScopeIsNull() {

		// arrange

		var factory = new ProviderFactory();

		// act & assert

		assertThatThrownBy(() -> factory.init(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("scope is required");
	}

	@Test
	void shouldStoreScopeWhenProvided() {

		// arrange

		var factory = new ProviderFactory();
		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());
		// act

		factory.init(scope);

		// assert

		assertThat(factory.getScope()).isSameAs(scope);
	}
}
