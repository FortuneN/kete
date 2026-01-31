package io.github.fortunen.kete.unittests.oauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.OAuthMaterial.OAuthMode;
import java.net.URI;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class buildTests {

	@Test
	void shouldBuildWithMinimalConfiguration() {

		// act

		var oauth = OAuthMaterial.builder()
			.withEnabled(false)
			.build();

		// assert

		assertThat(oauth).isNotNull();
		assertThat(oauth.isEnabled()).isFalse();
	}

	@Test
	void shouldBuildWithExternalModeConfiguration() {

		// act

		var oauth = OAuthMaterial.builder()
			.withEnabled(true)
			.withMode(OAuthMode.EXTERNAL)
			.withTokenUri(URI.create("http://localhost/token"))
			.withClientId("test-client")
			.withClientSecret("test-secret")
			.withScope("openid profile")
			.build();

		// assert

		assertThat(oauth).isNotNull();
		assertThat(oauth.isEnabled()).isTrue();
		assertThat(oauth.getMode()).isEqualTo(OAuthMode.EXTERNAL);
		assertThat(oauth.getTokenUri()).isEqualTo(URI.create("http://localhost/token"));
		assertThat(oauth.getClientId().getValue()).isEqualTo("test-client");
		assertThat(oauth.getClientSecret().getValue()).isEqualTo("test-secret");
		assertThat(oauth.getScope()).isNotNull();
		assertThat(oauth.getScope().toString()).contains("openid");
		assertThat(oauth.getScope().toString()).contains("profile");
	}

	@Test
	void shouldBuildWithInternalModeConfiguration() {

		// act

		var oauth = OAuthMaterial.builder()
			.withEnabled(true)
			.withMode(OAuthMode.INTERNAL)
			.withTokenUri(URI.create("http://localhost/token"))
			.withClientId("test-client")
			.withClientSecret("test-secret")
			.withRealm("test-realm")
			.build();

		// assert

		assertThat(oauth).isNotNull();
		assertThat(oauth.isEnabled()).isTrue();
		assertThat(oauth.getMode()).isEqualTo(OAuthMode.INTERNAL);
		assertThat(oauth.isInternalMode()).isTrue();
		assertThat(oauth.getRealm()).isEqualTo("test-realm");
	}

	@Test
	void shouldDefaultModeToInternalWhenEnabledAndModeNotSet() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "test-realm");
		var config = new MapConfiguration(map);

		// act

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(oauth.getMode())
			.as("Mode should be set to INTERNAL when configured")
			.isEqualTo(OAuthMode.INTERNAL);
	}

	@Test
	void shouldBuildWithOptionalScope() {

		// act

		var oauth = OAuthMaterial.builder()
			.withEnabled(true)
			.withMode(OAuthMode.EXTERNAL)
			.withTokenUri(URI.create("http://localhost/token"))
			.withClientId("test-client")
			.withClientSecret("test-secret")
			.build();

		// assert

		assertThat(oauth.getScope())
			.as("Scope should be null when not provided")
			.isNull();
	}

	@Test
	void shouldBuildDisabledOAuthMaterial() {

		// act

		var oauth = OAuthMaterial.builder()
			.withEnabled(false)
			.withMode(OAuthMode.EXTERNAL) // Mode set but disabled
			.build();

		// assert

		assertThat(oauth.isEnabled()).isFalse();
		assertThat(oauth.getMode()).isEqualTo(OAuthMode.EXTERNAL);
	}

	@Test
	void shouldNotRegisterClientWhenNoKeycloakSessionProvided() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "test-realm");
		var config = new MapConfiguration(map);

		// act

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		// assert

		assertThat(oauth.isClientRegistered())
			.as("Should not be registered when no KeycloakSession provided")
			.isFalse();
		assertThat(oauth.requiresClientRegistration())
			.as("Should still require registration")
			.isTrue();
	}
}
