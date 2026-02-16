package io.github.fortunen.kete.unittests.destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.DestinationConfig;

class initializeAuthenticationTypeTests {

	@Test
	void shouldSetAuthenticationTypeWhenPresent() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "oauth"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEqualTo("oauth");
		assertThat(destinationConfig.isHasAuthenticationType()).isTrue();
	}

	@Test
	void shouldSetHasAuthenticationTypeToFalseWhenBlank() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "   "));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEmpty();
		assertThat(destinationConfig.isHasAuthenticationType()).isFalse();
	}

	@Test
	void shouldSetHasAuthenticationTypeToFalseWhenEmpty() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", ""));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEmpty();
		assertThat(destinationConfig.isHasAuthenticationType()).isFalse();
	}

	@Test
	void shouldSetHasAuthenticationTypeToFalseWhenMissing() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEmpty();
		assertThat(destinationConfig.isHasAuthenticationType()).isFalse();
	}

	@Test
	void shouldTrimAuthenticationType() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "  oauth  "));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEqualTo("oauth");
		assertThat(destinationConfig.isHasAuthenticationType()).isTrue();
	}

	@Test
	void shouldLowercaseAuthenticationType() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "OAuth"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEqualTo("oauth");
	}

	@Test
	void shouldLowercaseAndTrimAuthenticationType() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "  Connection-String  "));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEqualTo("connection-string");
		assertThat(destinationConfig.isHasAuthenticationType()).isTrue();
	}

	@Test
	void shouldSetAuthenticationTypeForAccessKey() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "access-key"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEqualTo("access-key");
		assertThat(destinationConfig.isHasAuthenticationType()).isTrue();
	}

	@Test
	void shouldSetAuthenticationTypeForManagedIdentity() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "managed-identity"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEqualTo("managed-identity");
		assertThat(destinationConfig.isHasAuthenticationType()).isTrue();
	}

	@Test
	void shouldSetAuthenticationTypeForBasic() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test", "authentication-type", "basic"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getAuthenticationType()).isEqualTo("basic");
		assertThat(destinationConfig.isHasAuthenticationType()).isTrue();
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
}
