package io.github.fortunen.kete.unittests.utils.configurationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.keycloak.Config.Scope;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.utils.ConfigurationUtils;

class createConfigurationTests {

	@Test
	void shouldThrowWhenScopeIsNull() {

		// arrange

		var environment = new HashMap<String, String>();

		// act & assert

		assertThatThrownBy(() -> ConfigurationUtils.createConfiguration(null, environment))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("scope is required");
	}

	@Test
	void shouldThrowWhenEnvironmentIsNull() {

		// arrange

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());

		// act & assert

		assertThatThrownBy(() -> ConfigurationUtils.createConfiguration(scope, null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("environment is required");
	}

	@Test
	void shouldReturnNullWhenEnabledIsFalse() {

		// arrange

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());

		var environment = new HashMap<String, String>();
		environment.put("kete.enabled", "false");

		// act

		var result = ConfigurationUtils.createConfiguration(scope, environment);

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenLegacyEnabledIsFalse() {

		// arrange

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());

		var environment = new HashMap<String, String>();
		environment.put("enabled", "false");

		// act

		var result = ConfigurationUtils.createConfiguration(scope, environment);

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnConfigurationWithDefaultsWhenNoValuesProvided() {

		// arrange

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());

		var environment = new HashMap<String, String>();

		// act

		var result = ConfigurationUtils.createConfiguration(scope, environment);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getRoutes()).isEmpty();
	}

	@Test
	void shouldParseRoutesFromEnvironment() {

		// arrange

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());

		var environment = new HashMap<String, String>();
		environment.put(Constants.ID + ".routes.myroute.realm-matchers.prod", "list:master");
		environment.put(Constants.ID + ".routes.myroute.serializer.kind", "json");
		environment.put(Constants.ID + ".routes.myroute.destination.kind", "http");
		environment.put(Constants.ID + ".routes.myroute.destination.url", "http://localhost:8080");

		// act

		var result = ConfigurationUtils.createConfiguration(scope, environment);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getRoutes()).hasSize(1);
		assertThat(result.getRoutes()[0].getName()).isEqualTo("myroute");
	}

	@Test
	void shouldParseRoutesFromScope() {

		// arrange

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of(
			Constants.ID + ".routes.myroute.realm-matchers.prod",
			Constants.ID + ".routes.myroute.serializer.kind",
			Constants.ID + ".routes.myroute.destination.kind",
			Constants.ID + ".routes.myroute.destination.url"
		));
		when(scope.get(Constants.ID + ".routes.myroute.realm-matchers.prod")).thenReturn("list:master");
		when(scope.get(Constants.ID + ".routes.myroute.serializer.kind")).thenReturn("json");
		when(scope.get(Constants.ID + ".routes.myroute.destination.kind")).thenReturn("http");
		when(scope.get(Constants.ID + ".routes.myroute.destination.url")).thenReturn("http://localhost:8080");

		var environment = new HashMap<String, String>();

		// act

		var result = ConfigurationUtils.createConfiguration(scope, environment);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getRoutes()).hasSize(1);
		assertThat(result.getRoutes()[0].getName()).isEqualTo("myroute");
	}

	@Test
	void shouldOverrideScopeWithEnvironment() {

		// arrange

		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of(
			Constants.ID + ".routes.myroute.realm-matchers.prod",
			Constants.ID + ".routes.myroute.serializer.kind",
			Constants.ID + ".routes.myroute.destination.kind",
			Constants.ID + ".routes.myroute.destination.url"
		));
		when(scope.get(Constants.ID + ".routes.myroute.realm-matchers.prod")).thenReturn("list:master");
		when(scope.get(Constants.ID + ".routes.myroute.serializer.kind")).thenReturn("json");
		when(scope.get(Constants.ID + ".routes.myroute.destination.kind")).thenReturn("http");
		when(scope.get(Constants.ID + ".routes.myroute.destination.url")).thenReturn("http://original:8080");

		var environment = new HashMap<String, String>();
		environment.put(Constants.ID + ".routes.myroute.destination.url", "http://override:9090");

		// act

		var result = ConfigurationUtils.createConfiguration(scope, environment);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getRoutes()).hasSize(1);
		var destConfig = result.getRoutes()[0].getDestinationConfig();
		assertThat(destConfig).isNotNull();
	// Initialize the config so fields are populated from configuration
	destConfig.initialize();
	// Environment override should have updated the URL
	assertThat(destConfig.toString()).contains("override:9090");
}

@Test
void shouldSkipDisabledRoutes() {

	// arrange

	var scope = mock(Scope.class);
	when(scope.getPropertyNames()).thenReturn(Set.of());

	var environment = new HashMap<String, String>();
	environment.put(Constants.ID + ".routes.myroute.enabled", "false");
	environment.put(Constants.ID + ".routes.myroute.realm-matchers.prod", "list:master");
	environment.put(Constants.ID + ".routes.myroute.serializer.kind", "json");
	environment.put(Constants.ID + ".routes.myroute.destination.kind", "http");
	environment.put(Constants.ID + ".routes.myroute.destination.url", "http://localhost:8080");

	// act

	var result = ConfigurationUtils.createConfiguration(scope, environment);

	// assert

	assertThat(result).isNotNull();
	assertThat(result.getRoutes()).isEmpty();
}
}
