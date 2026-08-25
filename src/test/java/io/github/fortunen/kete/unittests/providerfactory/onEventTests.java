package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
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
	void shouldRegisterOnNewlyCreatedRealmWhenARouteAcceptsIt() {

		// arrange

		var factory = new ProviderFactory();
		factory.postInit(mock(KeycloakSessionFactory.class));

		var route = mock(Route.class);
		when(route.acceptRealm("new-realm")).thenReturn(true);
		factory.setSerializersWithRoutes(new SerializerRoutes[] { new SerializerRoutes(mock(Serializer.class), List.of(route)) });

		var realm = mock(RealmModel.class);
		when(realm.getName()).thenReturn("new-realm");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		var event = mock(RealmModel.RealmPostCreateEvent.class);
		when(event.getCreatedRealm()).thenReturn(realm);

		// act

		factory.onEvent(event);

		// assert

		verify(realm).setEventsEnabled(true);
		verify(realm).setAdminEventsEnabled(true);
		verify(realm).setAdminEventsDetailsEnabled(true);
		verify(realm).setEventsListeners(argThat(listeners -> listeners.contains(Constants.ID)));
	}

	@Test
	void shouldNotRegisterOnNewlyCreatedRealmWhenNoRouteAcceptsIt() {

		// arrange

		var factory = new ProviderFactory();
		factory.postInit(mock(KeycloakSessionFactory.class));

		var route = mock(Route.class);
		when(route.acceptRealm("new-realm")).thenReturn(false);
		factory.setSerializersWithRoutes(new SerializerRoutes[] { new SerializerRoutes(mock(Serializer.class), List.of(route)) });

		var realm = mock(RealmModel.class);
		when(realm.getName()).thenReturn("new-realm");
		when(realm.getEventsListenersStream()).thenReturn(Stream.of());

		var event = mock(RealmModel.RealmPostCreateEvent.class);
		when(event.getCreatedRealm()).thenReturn(realm);

		// act

		factory.onEvent(event);

		// assert

		verify(realm, never()).setEventsEnabled(true);
		verify(realm, never()).setEventsListeners(argThat(listeners -> listeners.contains(Constants.ID)));
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
