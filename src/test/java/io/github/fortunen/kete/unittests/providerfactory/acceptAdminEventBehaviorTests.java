package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import io.github.fortunen.kete.Configuration;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;

class acceptAdminEventBehaviorTests {

	@Test
	void shouldSerializeAdminEventWhenRouteAccepts() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());
		factory.setEventExecutor(Executors.newVirtualThreadPerTaskExecutor());

		var mockDestinationConfig = mock(DestinationConfig.class);
		var mockSerializer = mock(Serializer.class);
		var mockRoute = mock(Route.class);

		when(mockRoute.getDestinationConfig()).thenReturn(mockDestinationConfig);
		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.acceptRealm(anyString())).thenReturn(true);
		when(mockSerializer.getContentType()).thenReturn("application/json");

		when(mockRoute.acceptEvent("USER_CREATE")).thenReturn(true);
		when(mockSerializer.serialize(any(AdminEvent.class))).thenReturn("{}".getBytes());

		var routes = new SerializerRoutes(mockSerializer, List.of(mockRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		var event = createUserCreatedAdminEvent();

		// act

		factory.accept(event, false);

		// assert

		verify(mockSerializer, timeout(1000)).serialize(any(AdminEvent.class));

		// cleanup

		factory.close();
	}

	@Test
	void shouldSendAdminEventToDestination() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());
		factory.setEventExecutor(Executors.newVirtualThreadPerTaskExecutor());

		var mockSerializer = mock(Serializer.class);
		var mockRoute = mock(Route.class);

		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.acceptRealm(anyString())).thenReturn(true);
		when(mockSerializer.getContentType()).thenReturn("application/json");

		when(mockRoute.acceptEvent("USER_CREATE")).thenReturn(true);
		when(mockSerializer.serialize(any(AdminEvent.class))).thenReturn("{}".getBytes());

		var routes = new SerializerRoutes(mockSerializer, List.of(mockRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		var event = createUserCreatedAdminEvent();

		// act

		factory.accept(event, false);

		// assert

		verify(mockRoute, timeout(1000)).send(any(EventMessage.class));

		// cleanup

		factory.close();
	}

	@Test
	void shouldPopulateAdminEventMessageWithCorrectData() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());
		factory.setEventExecutor(Executors.newVirtualThreadPerTaskExecutor());

		var mockSerializer = mock(Serializer.class);
		var mockRoute = mock(Route.class);

		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.acceptRealm(anyString())).thenReturn(true);
		when(mockSerializer.getContentType()).thenReturn("application/json");

		when(mockRoute.acceptEvent("USER_CREATE")).thenReturn(true);
		when(mockSerializer.serialize(any(AdminEvent.class))).thenReturn("{\"op\":\"CREATE\"}".getBytes());

		var routes = new SerializerRoutes(mockSerializer, List.of(mockRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		var event = createUserCreatedAdminEvent();

		// act

		factory.accept(event, false);

		// assert

		var captor = org.mockito.ArgumentCaptor.forClass(EventMessage.class);
		verify(mockRoute, timeout(1000)).send(captor.capture());

		var message = captor.getValue();
		assertThat(message.eventId()).isEqualTo("admin-event-789");
		assertThat(message.eventType()).isEqualTo("USER_CREATE");
		assertThat(message.realm()).isEqualTo("master");
		assertThat(message.kind()).isEqualTo(Constants.ADMIN_EVENT);
		assertThat(message.operationType()).isEqualTo("CREATE");
		assertThat(message.resourceType()).isEqualTo("USER");

		// cleanup

		factory.close();
	}

	private AdminEvent createUserCreatedAdminEvent() {

		var event = mock(AdminEvent.class);
		when(event.getId()).thenReturn("admin-event-789");
		when(event.getRealmName()).thenReturn("master");
		when(event.getOperationType()).thenReturn(OperationType.CREATE);
		when(event.getResourceType()).thenReturn(ResourceType.USER);
		return event;
	}
}
