package io.github.fortunen.kete.unittests.destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.DestinationConfig;

class initializeKindAndConfigurationTests {

	// =========================================================================
	// Configuration Null Check
	// =========================================================================

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var destinationConfig = new TestDestinationConfig();
		destinationConfig.setKeycloakSession(mock(KeycloakSession.class));

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("configuration is required");
	}

	// =========================================================================
	// Kind Validation
	// =========================================================================

	@Test
	void shouldSetDestinationKindWhenPresent() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "kafka"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getDestinationKind()).isEqualTo("kafka");
	}

	@Test
	void shouldThrowWhenKindIsMissing() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("kind is required");
	}

	@Test
	void shouldThrowWhenKindIsEmpty() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", ""));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("kind is required");
	}

	@Test
	void shouldThrowWhenKindIsBlank() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "   "));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		var thrown = catchThrowable(() -> destinationConfig.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("kind is required");
	}

	@Test
	void shouldTrimKind() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "  mqtt  "));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getDestinationKind()).isEqualTo("mqtt");
	}

	@Test
	void shouldSetDestinationKindForVariousKinds() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "http"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getDestinationKind()).isEqualTo("http");
	}

	// =========================================================================
	// doInitialize is called
	// =========================================================================

	@Test
	void shouldCallDoInitialize() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test"));
		var destinationConfig = new TrackingTestDestinationConfig();
		destinationConfig.setConfiguration(config);
		destinationConfig.setKeycloakSession(mock(KeycloakSession.class));

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.doInitializeCalled).isTrue();
	}

	// Helper methods

	private DestinationConfig createTestDestinationConfig(MapConfiguration config) {

		var destinationConfig = new TestDestinationConfig();
		destinationConfig.setConfiguration(config);
		destinationConfig.setKeycloakSession(mock(KeycloakSession.class));
		return destinationConfig;
	}

	private static class TestDestinationConfig extends DestinationConfig {

		@Override
		protected void doInitialize() {
		}
	}

	private static class TrackingTestDestinationConfig extends DestinationConfig {

		boolean doInitializeCalled = false;

		@Override
		protected void doInitialize() {
			doInitializeCalled = true;
		}
	}
}
