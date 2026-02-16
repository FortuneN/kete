package io.github.fortunen.kete.unittests.destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.DestinationConfig;

class initializeTlsTests {

	@Test
	void shouldBuildTlsMaterialFromConfiguration() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls()).isNotNull();
	}

	@Test
	void shouldBuildTlsDisabledByDefault() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls().isEnabled()).isFalse();
	}

	@Test
	void shouldBuildTlsEnabledWhenConfigured() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "test");
		map.put("tls.enabled", true);
		var config = new MapConfiguration(map);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls().isEnabled()).isTrue();
		assertThat(destinationConfig.getTls().getKeyStoreAndTrustStoreSSLContext()).isNotNull();
	}

	@Test
	void shouldBuildTlsWithCustomVersionWhenConfigured() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "test");
		map.put("tls.enabled", true);
		map.put("tls.version", "TLSv1.3");
		var config = new MapConfiguration(map);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls().getVersion()).isEqualTo("TLSv1.3");
	}

	@Test
	void shouldBuildTlsWithDefaultVersionWhenNotConfigured() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "test");
		map.put("tls.enabled", true);
		var config = new MapConfiguration(map);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls().getVersion()).isEqualTo("TLS");
	}

	@Test
	void shouldHaveNullSslContextWhenTlsNotEnabled() {

		// arrange

		var config = new MapConfiguration(Map.of("kind", "test"));
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls().getKeyStoreAndTrustStoreSSLContext()).isNull();
	}

	@Test
	void shouldBuildTlsWithTrustStoreTypeWhenConfigured() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "test");
		map.put("tls.enabled", true);
		map.put("tls.trust-store.type", "PKCS12");
		var config = new MapConfiguration(map);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls().getTrustStoreType()).isEqualTo("PKCS12");
	}

	@Test
	void shouldBuildTlsWithKeyStoreTypeWhenConfigured() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "test");
		map.put("tls.enabled", true);
		map.put("tls.key-store.type", "PKCS12");
		var config = new MapConfiguration(map);
		var destinationConfig = createTestDestinationConfig(config);

		// act

		destinationConfig.initialize();

		// assert

		assertThat(destinationConfig.getTls().getKeyStoreType()).isEqualTo("PKCS12");
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
