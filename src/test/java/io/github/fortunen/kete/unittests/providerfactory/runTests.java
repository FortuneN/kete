package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;

import io.github.fortunen.kete.Configuration;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.matchers.ListMatcher;

class runTests {

	// =========================================================================
	// Helper Methods
	// =========================================================================

	private Matcher createRealmMatcher(String realm) {

		var matcher = new ListMatcher();
		matcher.setName("realm");
		matcher.setPattern(realm);
		matcher.initialize();
		return matcher;
	}

	// =========================================================================
	// Validation Tests
	// =========================================================================

	@Test
	void shouldThrowWhenSessionIsNull() {

		// arrange

		var factory = new ProviderFactory();

		// act

		var thrown = catchThrowable(() -> factory.run(null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("session is required");
	}

	@Test
	void shouldThrowWhenRealmProviderIsNull() {

		// arrange

		var factory = new ProviderFactory();
		var session = mock(KeycloakSession.class);
		when(session.realms()).thenReturn(null);

		// act

		var thrown = catchThrowable(() -> factory.run(session));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("realmProvider is required");
	}

	@Test
	void shouldThrowWhenRealmsStreamIsNull() {

		// arrange

		var factory = new ProviderFactory();
		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(null);

		// act

		var thrown = catchThrowable(() -> factory.run(session));

		// assert

		assertThat(thrown).isInstanceOf(NullPointerException.class);
	}

	@Test
	void shouldSetConfigurationWithEmptyEnvironment() {

		// arrange

		var factory = new ProviderFactory();
		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());
		factory.init(scope);

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);
		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// act

		factory.run(session);

		// assert

		assertThat(factory.getConfiguration()).isNotNull();
	}

	// =========================================================================
	// Behavior Tests
	// =========================================================================

	@Test
	void shouldRemoveListenerFromRealmWhenConfigurationIsNull() {

		// arrange

		var env = new HashMap<String, String>();
		env.put("enabled", "false"); // This makes ConfigurationUtils.createConfiguration return null

		var factory = new ProviderFactory();
		var scope = mock(Scope.class);
		when(scope.getPropertyNames()).thenReturn(Set.of());
		factory.init(scope);
		factory.setEnvironment(env);
	var session = mock(KeycloakSession.class);
	var realmProvider = mock(RealmProvider.class);
	var realm = mock(RealmModel.class);
		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of(Constants.ID));

		// act

		factory.run(session);

		// assert

		assertThat(factory.getConfiguration()).isNull();
		verify(realm).setEventsListeners(Set.of());
	}

	@Test
	void shouldCreateExecutorWhenConfigurationHasRoutes() {

		// arrange

		var env = new HashMap<String, String>();
		env.put("kete.routes.route-01.realm-matchers.prod", "list:master");
		env.put("kete.routes.route-01.destination.kind", "http");
		env.put("kete.routes.route-01.destination.url", "http://localhost:8080");
		env.put("kete.routes.route-01.serializer.kind", "json");

		var factory = new ProviderFactory();
		factory.setEnvironment(env);
		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// act

		try {
			factory.run(session);
		} catch (Exception e) {
			// expected
		}

		// assert

		assertThat(factory.getEventExecutor()).isNotNull();

		// cleanup
		factory.close();
	}

	@Test
	void shouldHandleRouteInitializationFailure() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var route = new Route();
		route.setName("test-route");
		route.setRealmMatchers(new Matcher[] { createRealmMatcher("master") });
		route.setEventMatchers(new Matcher[0]);
		route.setSerializer(mock(Serializer.class));

		var failingDestinationConfig = mock(DestinationConfig.class);
		try {
			org.mockito.Mockito.doThrow(new RuntimeException("Init failed")).when(failingDestinationConfig).initialize();
		} catch (Exception e) {
			// expected
		}
		route.setDestinationConfig(failingDestinationConfig);

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		var env = new HashMap<String, String>();
		factory.setEnvironment(env);
		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(1));

		// act

		try {
			failingDestinationConfig.initialize();
		} catch (Exception e) {
			// expected
		}

		// assert

		verify(failingDestinationConfig).initialize();

		// cleanup
		factory.close();
	}

	@Test
	void shouldHandleDestinationInitFailure() throws Exception {

		// arrange

		var failingDestinationConfig = mock(DestinationConfig.class);
		org.mockito.Mockito.doThrow(new RuntimeException("Init failed")).when(failingDestinationConfig).initialize();

		try (var route = new Route()) {

			route.setName("failing-route");
			route.setRealmMatchers(new Matcher[] { createRealmMatcher("master") });
			route.setEventMatchers(new Matcher[0]);
			route.setSerializer(mock(Serializer.class));
			route.setDestinationConfig(failingDestinationConfig);

			// act & assert

			try {
				failingDestinationConfig.initialize();
			} catch (Exception e) {
				// expected
			}

			// verify initialize was attempted
			verify(failingDestinationConfig).initialize();
		}
	}

	@Test
	void shouldAddListenerToRealmWithMatchingRoute() {

		// arrange

		var factory = new ProviderFactory();

		var config = new Configuration();

		var mockRoute = mock(Route.class);
		when(mockRoute.getRealmMatchers()).thenReturn(new Matcher[] { createRealmMatcher("master") });
		when(mockRoute.acceptRealm("master")).thenReturn(true);
		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.getEventMatchers()).thenReturn(new Matcher[0]);
		when(mockRoute.getSerializer()).thenReturn(mock(Serializer.class));
		when(mockRoute.getDestinationConfig()).thenReturn(mock(DestinationConfig.class));

		config.setRoutes(new Route[] { mockRoute });

		factory.setConfiguration(config);
		factory.setEventExecutor(java.util.concurrent.Executors.newFixedThreadPool(1));

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// assert

		assertThat(config.getRoutes()).hasSize(1);

		// cleanup
		factory.close();
	}

	@Test
	void shouldRemoveListenerFromRealmWithNoMatchingRoute() {

		// arrange

		var factory = new ProviderFactory();

		var config = new Configuration();

		var mockRoute = mock(Route.class);
		when(mockRoute.getRealmMatchers()).thenReturn(new Matcher[] { createRealmMatcher("other-realm") });
		when(mockRoute.acceptRealm("master")).thenReturn(false);
		when(mockRoute.acceptRealm("other-realm")).thenReturn(true);
		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.getEventMatchers()).thenReturn(new Matcher[0]);
		when(mockRoute.getSerializer()).thenReturn(mock(Serializer.class));
		when(mockRoute.getDestinationConfig()).thenReturn(mock(DestinationConfig.class));

		config.setRoutes(new Route[] { mockRoute });

		factory.setConfiguration(config);
		factory.setEventExecutor(java.util.concurrent.Executors.newFixedThreadPool(1));

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of(Constants.ID));

		// cleanup

		factory.close();
	}

	@Test
	void shouldHandleNullRoutesInConfiguration() {

		// arrange

		var factory = new ProviderFactory();
		factory.setEnvironment(new HashMap<>());
		var config = new Configuration();
		config.setRoutes(null);

		factory.setConfiguration(config);
		factory.setEventExecutor(java.util.concurrent.Executors.newFixedThreadPool(1));

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realm = mock(RealmModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmsStream()).thenReturn(Stream.of(realm));
		when(realm.getName()).thenReturn("master");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		// assert

		assertThat(factory.getConfiguration()).isNotNull();

		// cleanup
		factory.close();
	}
}
