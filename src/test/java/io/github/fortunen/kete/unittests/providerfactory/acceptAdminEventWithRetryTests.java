package io.github.fortunen.kete.unittests.providerfactory;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import io.github.fortunen.kete.Configuration;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.ProviderFactory;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.SerializerRoutes;

class acceptAdminEventWithRetryTests {

	@Test
	void shouldExecuteWithRetryWhenRetryIsConfigured() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var serializer = mock(Serializer.class);

		when(serializer.getContentType()).thenReturn("application/json");
		when(serializer.serialize(any(AdminEvent.class))).thenReturn("{}".getBytes());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("admin-retry-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("USER_CREATE")).thenReturn(true);

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var adminEvent = new AdminEvent();
		adminEvent.setId("admin-event-with-retry");
		adminEvent.setRealmId("master");
		adminEvent.setRealmName("master");
		adminEvent.setResourceType(ResourceType.USER);
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setTime(System.currentTimeMillis());

		// act

		factory.accept(adminEvent, false);
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
		when(serializer.serialize(any(AdminEvent.class))).thenReturn("{}".getBytes());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("admin-no-retry-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("CLIENT_DELETE")).thenReturn(true);

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var adminEvent = new AdminEvent();
		adminEvent.setId("admin-event-no-retry");
		adminEvent.setRealmId("master");
		adminEvent.setRealmName("master");
		adminEvent.setResourceType(ResourceType.CLIENT);
		adminEvent.setOperationType(OperationType.DELETE);
		adminEvent.setTime(System.currentTimeMillis());

		// act

		factory.accept(adminEvent, false);
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
	void shouldHandleSendFailureGracefullyWithoutRetry() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var serializer = mock(Serializer.class);

		when(serializer.getContentType()).thenReturn("application/json");
		when(serializer.serialize(any(AdminEvent.class))).thenReturn("{}".getBytes());

		var route = mock(Route.class);
		when(route.getName()).thenReturn("admin-failing-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("REALM_UPDATE")).thenReturn(true);

		doThrow(new RuntimeException("Send failed"))
			.when(route).send(any());

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var adminEvent = new AdminEvent();
		adminEvent.setId("admin-event-send-failure");
		adminEvent.setRealmId("master");
		adminEvent.setRealmName("master");
		adminEvent.setResourceType(ResourceType.REALM);
		adminEvent.setOperationType(OperationType.UPDATE);
		adminEvent.setTime(System.currentTimeMillis());

		// act

		factory.accept(adminEvent, false);
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
	void shouldHandleSerializationFailureGracefully() throws Exception {

		// arrange

		var factory = new ProviderFactory();

		var serializer = mock(Serializer.class);

		when(serializer.getContentType()).thenReturn("application/json");
		when(serializer.serialize(any(AdminEvent.class)))
			.thenThrow(new RuntimeException("Serialization failed"));

		var route = mock(Route.class);
		when(route.getName()).thenReturn("admin-serialize-fail-route");
		when(route.acceptRealm("master")).thenReturn(true);
		when(route.acceptEvent("GROUP_CREATE")).thenReturn(true);

		var config = new Configuration();
		config.setRoutes(new Route[] { route });

		factory.setConfiguration(config);
		factory.setEventExecutor(Executors.newFixedThreadPool(2));

		var routes = new SerializerRoutes(serializer, List.of(route));
		factory.setSerializersWithRoutes(
			new SerializerRoutes[] { routes }
		);

		var adminEvent = new AdminEvent();
		adminEvent.setId("admin-event-serialize-fail");
		adminEvent.setRealmId("master");
		adminEvent.setRealmName("master");
		adminEvent.setResourceType(ResourceType.GROUP);
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setTime(System.currentTimeMillis());

		// act

		factory.accept(adminEvent, false);
		await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(() -> {
			try {
				verify(route, never()).send(any(EventMessage.class));
				return true;
			} catch (AssertionError e) {
				return false;
			}
		});

		// cleanup

		factory.close();
	}
}
