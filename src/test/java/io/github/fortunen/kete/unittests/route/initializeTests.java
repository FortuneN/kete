package io.github.fortunen.kete.unittests.route;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.Serializer;

@SuppressWarnings("unchecked")
public class initializeTests extends TestBase {

	@Test
	public void shouldThrowWhenSessionIsNull() {

		// arrange

		route = new Route();
		route.setSerializer(mock(Serializer.class));
		route.setDestinationConfig(mock(DestinationConfig.class));

		// act

		var thrown = catchThrowable(() -> route.initialize(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("session is required");
	}

	@Test
	public void shouldThrowWhenSerializerIsNull() {

		// arrange

		route = new Route();
		route.setDestinationConfig(mock(DestinationConfig.class));
		var session = mock(KeycloakSession.class);

		// act

		var thrown = catchThrowable(() -> route.initialize(session));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("serializer is required");
	}

	@Test
	public void shouldThrowWhenDestinationIsNull() {

		// arrange

		route = new Route();
		route.setSerializer(mock(Serializer.class));
		var session = mock(KeycloakSession.class);

		// act

		var thrown = catchThrowable(() -> route.initialize(session));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("destinationConfig is required");
	}

	@Test
	public void shouldSetKeycloakSessionOnDestinationConfig() throws Exception {

		// arrange

		route = new Route();
		route.setSerializer(mock(Serializer.class));
		var destinationConfig = mock(DestinationConfig.class);
		route.setDestinationConfig(destinationConfig);

		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var session = mock(KeycloakSession.class);

		// act

		route.initialize(session);

		// assert

		verify(destinationConfig).setKeycloakSession(session);
	}

	@Test
	public void shouldInitializeDestinationConfig() throws Exception {

		// arrange

		route = new Route();
		route.setSerializer(mock(Serializer.class));
		var destinationConfig = mock(DestinationConfig.class);
		route.setDestinationConfig(destinationConfig);

		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var session = mock(KeycloakSession.class);

		// act

		route.initialize(session);

		// assert

		verify(destinationConfig).initialize();
	}

	@Test
	public void shouldInitializeDestinationConfigAndPool() throws Exception {

		// arrange

		route = new Route();
		route.setSerializer(mock(Serializer.class));
		var destinationConfig = mock(DestinationConfig.class);
		route.setDestinationConfig(destinationConfig);

		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var session = mock(KeycloakSession.class);

		// act

		route.initialize(session);

		// assert
		// Matchers are initialized by RouteUtils.createRoute(), not Route.initialize()
		// Route.initialize() only initializes destination config and tests pool

		verify(destinationConfig).setKeycloakSession(session);
		verify(destinationConfig).initialize();
	}

	@Test
	public void shouldSucceedWithNoMatchers() throws Exception {

		// arrange

		route = new Route();
		route.setSerializer(mock(Serializer.class));
		var destinationConfig = mock(DestinationConfig.class);
		route.setDestinationConfig(destinationConfig);

		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var session = mock(KeycloakSession.class);

		// act

		route.initialize(session);

		// assert

		verify(destinationConfig).initialize();
	}
}
