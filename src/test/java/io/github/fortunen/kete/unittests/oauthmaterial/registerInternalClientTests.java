package io.github.fortunen.kete.unittests.oauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.OAuthMaterial.OAuthMode;
import java.net.URI;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.junit.jupiter.api.Test;

class registerInternalClientTests {

	@Test
	void shouldThrowWhenSessionIsNull() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "test-realm");
		var config = new MapConfiguration(map);

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		// act & assert

		assertThatThrownBy(() -> oauth.registerInternalClient(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("session is required");
	}

	@Test
	void shouldReturnTrueWhenNotEnabled() {

		// arrange

		var oauth = OAuthMaterial.builder()
			.withEnabled(false)
			.build();

		var session = mock(KeycloakSession.class);

		// act

		var result = oauth.registerInternalClient(session);

		// assert

		assertThat(result)
			.as("Should return true when OAuth is not enabled (nothing to register)")
			.isTrue();
	}

	@Test
	void shouldReturnTrueWhenExternalMode() {

		// arrange

		var oauth = OAuthMaterial.builder()
			.withEnabled(true)
			.withMode(OAuthMode.EXTERNAL)
			.withTokenUri(URI.create("http://localhost/token"))
			.withClientId("test-client")
			.withClientSecret("test-secret")
			.build();

		var session = mock(KeycloakSession.class);

		// act

		var result = oauth.registerInternalClient(session);

		// assert

		assertThat(result)
			.as("Should return true when external mode (nothing to register internally)")
			.isTrue();
	}

	@Test
	void shouldReturnTrueWhenAlreadyRegistered() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "test-realm");
		var config = new MapConfiguration(map);

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		oauth.markClientRegistered(); // Already registered

		var session = mock(KeycloakSession.class);

		// act

		var result = oauth.registerInternalClient(session);

		// assert

		assertThat(result)
			.as("Should return true when client is already registered")
			.isTrue();
	}

	@Test
	void shouldReturnFalseWhenRealmNotFound() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "nonexistent-realm");
		var config = new MapConfiguration(map);

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmByName("nonexistent-realm")).thenReturn(null);

		// act

		var result = oauth.registerInternalClient(session);

		// assert

		assertThat(result)
			.as("Should return false when realm is not found")
			.isFalse();
	}

	@Test
	void shouldReturnTrueWhenClientAlreadyExistsInRealm() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "test-realm");
		var config = new MapConfiguration(map);

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		var session = mock(KeycloakSession.class);
		var realmProvider = mock(RealmProvider.class);
		var realmModel = mock(RealmModel.class);
		var existingClient = mock(org.keycloak.models.ClientModel.class);

		when(session.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealmByName("test-realm")).thenReturn(realmModel);
		when(realmModel.getClientByClientId("kete-oauth-client")).thenReturn(existingClient);

		// act

		var result = oauth.registerInternalClient(session);

		// assert

		assertThat(result)
			.as("Should return true when client already exists in realm")
			.isTrue();
		assertThat(oauth.isClientRegistered())
			.as("Should mark as registered when client already exists")
			.isTrue();
	}
}
