package io.github.fortunen.kete.unittests.natsauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.NatsAuthMaterial;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class initializeTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var authMaterial = new NatsAuthMaterial();

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldThrowWhenAuthenticationMethodIsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var config = new MapConfiguration(new HashMap<>());

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("authentication-method is required (none, username-and-password, token, nkey, credentials-file-path, credentials-file-text, credentials-file-base64)");
	}

	@Test
	void shouldThrowWhenAuthenticationMethodIsInvalid() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "invalid-method");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("invalid authentication-method: invalid-method (valid values: none, username-and-password, token, nkey, credentials-file-path, credentials-file-text, credentials-file-base64)");
	}

	@Test
	void shouldInitializeWithAuthNone() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "none");
		var config = new MapConfiguration(map);

		// act

		authMaterial.initialize(config);

		// assert

		assertThat(authMaterial.getAuthenticationMethod()).isEqualTo("none");
	}

	@Test
	void shouldInitializeWithUsernameAndPassword() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "username-and-password");
		map.put("username", "testuser");
		map.put("password", "testpass");
		var config = new MapConfiguration(map);

		// act

		authMaterial.initialize(config);

		// assert

		assertThat(authMaterial.getAuthenticationMethod()).isEqualTo("username-and-password");
		assertThat(authMaterial.getUsername()).isEqualTo("testuser");
		assertThat(authMaterial.getPassword()).isEqualTo("testpass");
	}

	@Test
	void shouldThrowWhenUsernameIsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "username-and-password");
		map.put("password", "testpass");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("username is required when authentication-method is username-and-password");
	}

	@Test
	void shouldThrowWhenPasswordIsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "username-and-password");
		map.put("username", "testuser");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("password is required when authentication-method is username-and-password");
	}

	@Test
	void shouldInitializeWithToken() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "token");
		map.put("token", "my-secret-token");
		var config = new MapConfiguration(map);

		// act

		authMaterial.initialize(config);

		// assert

		assertThat(authMaterial.getAuthenticationMethod()).isEqualTo("token");
		assertThat(authMaterial.getToken()).isEqualTo("my-secret-token");
	}

	@Test
	void shouldThrowWhenTokenIsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "token");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("token is required when authentication-method is token");
	}

	@Test
	void shouldInitializeWithNKey() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "nkey");
		map.put("nkey-seed", "SUACSSL3UAHUDXKFSNVUZRF5UHPMWZ6BFDTJ7M6USDXIEDNPPQYYYCU3VY");
		var config = new MapConfiguration(map);

		// act

		authMaterial.initialize(config);

		// assert

		assertThat(authMaterial.getAuthenticationMethod()).isEqualTo("nkey");
		assertThat(authMaterial.getNkeySeed()).isEqualTo("SUACSSL3UAHUDXKFSNVUZRF5UHPMWZ6BFDTJ7M6USDXIEDNPPQYYYCU3VY");
	}

	@Test
	void shouldThrowWhenNkeySeedIsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "nkey");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("nkey-seed is required when authentication-method is nkey");
	}

	@Test
	void shouldInitializeWithCredentialsFileText() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var credsContent = "-----BEGIN NATS USER JWT-----\neyJ0eXAiOiJKV1QiLCJhbGciOiJlZDI1NTE5LW5rZXkifQ.eyJqdGkiOiJBQkNERUYiLCJpYXQiOjE3MDQwNjcyMDAsImlzcyI6IkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsInN1YiI6IlVBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsIm5hdHMiOnt9fQ.SIGNATURE\n------END NATS USER JWT------\n-----BEGIN USER NKEY SEED-----\nSUACSSL3UAHUDXKFSNVUZRF5UHPMWZ6BFDTJ7M6USDXIEDNPPQYYYCU3VY\n------END USER NKEY SEED------";
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "credentials-file-text");
		map.put("credentials-file-text", credsContent);
		var config = new MapConfiguration(map);

		// act

		authMaterial.initialize(config);

		// assert

		assertThat(authMaterial.getAuthenticationMethod()).isEqualTo("credentials-file-text");
		assertThat(authMaterial.getCredentialsFileContent()).isEqualTo(credsContent);
	}

	@Test
	void shouldThrowWhenCredentialsFileTextIsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "credentials-file-text");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials-file-text is required when authentication-method is credentials-file-text");
	}

	@Test
	void shouldInitializeWithCredentialsFileBase64() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var credsContent = "-----BEGIN NATS USER JWT-----\neyJ0eXAiOiJKV1QiLCJhbGciOiJlZDI1NTE5LW5rZXkifQ.eyJqdGkiOiJBQkNERUYiLCJpYXQiOjE3MDQwNjcyMDAsImlzcyI6IkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsInN1YiI6IlVBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsIm5hdHMiOnt9fQ.SIGNATURE\n------END NATS USER JWT------\n-----BEGIN USER NKEY SEED-----\nSUACSSL3UAHUDXKFSNVUZRF5UHPMWZ6BFDTJ7M6USDXIEDNPPQYYYCU3VY\n------END USER NKEY SEED------";
		var credsBase64 = Base64.getEncoder().encodeToString(credsContent.getBytes(StandardCharsets.UTF_8));
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "credentials-file-base64");
		map.put("credentials-file-base64", credsBase64);
		var config = new MapConfiguration(map);

		// act

		authMaterial.initialize(config);

		// assert

		assertThat(authMaterial.getAuthenticationMethod()).isEqualTo("credentials-file-base64");
		assertThat(authMaterial.getCredentialsFileContent()).isEqualTo(credsContent);
	}

	@Test
	void shouldThrowWhenCredentialsFileBase64IsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "credentials-file-base64");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials-file-base64 is required when authentication-method is credentials-file-base64");
	}

	@Test
	void shouldThrowWhenCredentialsFilePathIsMissing() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "credentials-file-path");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> authMaterial.initialize(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("credentials-file-path is required when authentication-method is credentials-file-path");
	}
}
