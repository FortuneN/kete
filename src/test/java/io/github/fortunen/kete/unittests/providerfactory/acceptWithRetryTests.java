package io.github.fortunen.kete.unittests.providerfactory;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.fortunen.kete.Configuration;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.mockito.ArgumentMatchers;

class acceptWithRetryTests {

	@Test
	void shouldExecuteWithRetryWhenRetryIsConfigured() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var serializer = mock(Serializer.class);

		when(serializer.getContentType()).thenReturn("application/json");
		when(serializer.serialize(ArgumentMatchers.<Event>any())).thenReturn("{}".getBytes());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("retry-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("LOGIN")).thenReturn(true);

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var event = new Event();
		event.setId("event-with-retry");
		event.setRealmId("master");
		event.setRealmName("master");
		event.setType(EventType.LOGIN);
		event.setTime(System.currentTimeMillis());

		// act

		factory.accept(event);
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				verify(route, atLeastOnce()).send(any(EventMessage.class));
				return true;
			} catch (AssertionError e) {
				return false;
			}
		});

		// cleanup

		factory.close();
	}

	@Test
	void shouldRetryOnFailureWhenRetryConfigured() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var serializer = mock(Serializer.class);

		when(serializer.getContentType()).thenReturn("application/json");
		when(serializer.serialize(ArgumentMatchers.<Event>any())).thenReturn("{}".getBytes());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("retry-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("LOGIN")).thenReturn(true);

		doThrow(new RuntimeException("First attempt failed"))
			.doNothing()
			.when(route).send(any());

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var event = new Event();
		event.setId("retry-event");
		event.setRealmId("master");
		event.setRealmName("master");
		event.setType(EventType.LOGIN);
		event.setTime(System.currentTimeMillis());

		// act

		factory.accept(event);
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				verify(route, atLeastOnce()).send(any(EventMessage.class));
				return true;
			} catch (AssertionError e) {
				return false;
			}
		});

		// cleanup

		factory.close();
	}

	@Test
	void shouldSendWithoutRetryWhenRetryIsNull() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var serializer = mock(Serializer.class);

		when(serializer.getContentType()).thenReturn("application/json");
		when(serializer.serialize(ArgumentMatchers.<Event>any())).thenReturn("{}".getBytes());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("no-retry-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("LOGOUT")).thenReturn(true);

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var event = new Event();
		event.setId("no-retry-event");
		event.setRealmId("master");
		event.setRealmName("master");
		event.setType(EventType.LOGOUT);
		event.setTime(System.currentTimeMillis());

		// act

		factory.accept(event);
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				verify(route, atLeastOnce()).send(any(EventMessage.class));
				return true;
			} catch (AssertionError e) {
				return false;
			}
		});

		// cleanup

		factory.close();
	}

	@Test
	void shouldHandleSendFailureGracefully() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var serializer = mock(Serializer.class);

		when(serializer.getContentType()).thenReturn("application/json");
		when(serializer.serialize(ArgumentMatchers.<Event>any())).thenReturn("{}".getBytes());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("failing-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("LOGIN")).thenReturn(true);

		doThrow(new RuntimeException("Send failed")).when(route).send(any());

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var event = new Event();
		event.setId("failing-event");
		event.setRealmId("master");
		event.setRealmName("master");
		event.setType(EventType.LOGIN);
		event.setTime(System.currentTimeMillis());

		// act

		factory.accept(event);
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				verify(route).send(any(EventMessage.class));
				return true;
			} catch (AssertionError e) {
				return false;
			}
		});

		// cleanup

		factory.close();
	}
}
