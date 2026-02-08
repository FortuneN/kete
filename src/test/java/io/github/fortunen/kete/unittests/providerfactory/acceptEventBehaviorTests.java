package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.mockito.ArgumentCaptor;

import io.github.fortunen.kete.Configuration;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;

class acceptEventBehaviorTests {

	@Test
	void shouldSerializeEventWhenRouteAccepts() throws Exception {

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

		when(mockRoute.acceptEvent("LOGIN")).thenReturn(true);
		when(mockSerializer.serialize(any(Event.class))).thenReturn("{}".getBytes());

		var routes = new SerializerRoutes(mockSerializer, List.of(mockRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		var event = createLoginEvent();

		// act

		factory.accept(event);

		// assert

		verify(mockSerializer, timeout(1000)).serialize(any(Event.class));

		// cleanup

		factory.close();
	}

	@Test
	void shouldSendMessageToDestinationWhenRouteAccepts() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());
		factory.setEventExecutor(Executors.newVirtualThreadPerTaskExecutor());

		var mockSerializer = mock(Serializer.class);
		var mockRoute = mock(Route.class);

		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.acceptRealm(anyString())).thenReturn(true);
		when(mockSerializer.getContentType()).thenReturn("application/json");

		when(mockRoute.acceptEvent("LOGIN")).thenReturn(true);
		when(mockSerializer.serialize(any(Event.class))).thenReturn("{}".getBytes());

		var routes = new SerializerRoutes(mockSerializer, List.of(mockRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		var event = createLoginEvent();

		// act

		factory.accept(event);

		// assert

		verify(mockRoute, timeout(1000)).send(any(EventMessage.class));

		// cleanup

		factory.close();
	}

	@Test
	void shouldNotSendMessageWhenRouteRejects() throws Exception {

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

		when(mockRoute.acceptEvent("LOGIN")).thenReturn(false);
		when(mockSerializer.serialize(any(Event.class))).thenReturn("{}".getBytes());

		var routes = new SerializerRoutes(mockSerializer, List.of(mockRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		var event = createLoginEvent();

		// act

		factory.accept(event);
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				verify(mockRoute, never()).send(any(EventMessage.class));
				return true;
			} catch (AssertionError e) {
				return false;
			}
		});

		// cleanup

		factory.close();
	}

	@Test
	void shouldPopulateEventMessageWithCorrectData() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());
		factory.setEventExecutor(Executors.newVirtualThreadPerTaskExecutor());

		var mockSerializer = mock(Serializer.class);
		var mockRoute = mock(Route.class);

		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.acceptRealm(anyString())).thenReturn(true);
		when(mockSerializer.getContentType()).thenReturn("application/json");

		when(mockRoute.acceptEvent("LOGIN")).thenReturn(true);
		when(mockSerializer.serialize(any(Event.class))).thenReturn("{\"type\":\"LOGIN\"}".getBytes());

		var routes = new SerializerRoutes(mockSerializer, List.of(mockRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes });

		var event = createLoginEvent();

		// act

		factory.accept(event);

		// assert

		var captor = ArgumentCaptor.forClass(EventMessage.class);
		verify(mockRoute, timeout(1000)).send(captor.capture());

		var message = captor.getValue();
		assertThat(message.eventId()).isEqualTo("event-123");
		assertThat(message.eventType()).isEqualTo("LOGIN");
		assertThat(message.realm()).isEqualTo("master");
		assertThat(message.kind()).isEqualTo(Constants.EVENT);
		assertThat(message.contentType()).isEqualTo("application/json");
		assertThat(message.eventBody()).isNotEmpty();

		// cleanup

		factory.close();
	}

	@Test
	void shouldContinueProcessingWhenSerializerFails() throws Exception {

		// arrange

		var factory = new ProviderFactory();
		factory.setConfiguration(new Configuration());
		factory.setEventExecutor(Executors.newVirtualThreadPerTaskExecutor());

		var mockSerializer = mock(Serializer.class);
		var mockRoute = mock(Route.class);

		when(mockRoute.getName()).thenReturn("test-route");
		when(mockRoute.acceptRealm(anyString())).thenReturn(true);
		when(mockSerializer.getContentType()).thenReturn("application/json");

		var failingSerializer = mock(Serializer.class);
		when(failingSerializer.serialize(any(Event.class))).thenThrow(new RuntimeException("Serialization failed"));
		when(failingSerializer.getContentType()).thenReturn("application/xml");

		var workingSerializer = mockSerializer;
		when(workingSerializer.serialize(any(Event.class))).thenReturn("{}".getBytes());

		var failingRoute = mock(Route.class);
		when(failingRoute.acceptEvent("LOGIN")).thenReturn(true);
		when(failingRoute.getName()).thenReturn("failing-route");

		var workingRoute = mockRoute;
		when(workingRoute.acceptEvent("LOGIN")).thenReturn(true);

		var routes1 = new SerializerRoutes(failingSerializer, List.of(failingRoute));
		var routes2 = new SerializerRoutes(workingSerializer, List.of(workingRoute));
		factory.setSerializersWithRoutes(new SerializerRoutes[] { routes1, routes2 });

		var event = createLoginEvent();

		// act

		factory.accept(event);

		// assert

		verify(mockRoute, timeout(1000)).send(any(EventMessage.class));

		// cleanup

		factory.close();
	}

	private Event createLoginEvent() {

		var event = new Event();
		event.setId("event-123");
		event.setType(EventType.LOGIN);
		event.setRealmName("master");
		event.setClientId("test-client");
		event.setUserId("user-456");
		event.setTime(System.currentTimeMillis());
		return event;
	}
}
