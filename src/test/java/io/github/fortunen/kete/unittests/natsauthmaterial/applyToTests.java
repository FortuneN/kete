package io.github.fortunen.kete.unittests.natsauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.NatsAuthMaterial;
import io.nats.client.Options;

class applyToTests {

	@Test
	void shouldThrowWhenBuilderIsNull() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "none");
		var config = new MapConfiguration(map);
		authMaterial.initialize(config);

		// act & assert

		assertThatThrownBy(() -> authMaterial.applyTo(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("builder is required");
	}

	@Test
	void shouldApplyNoAuthForAuthNone() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "none");
		var config = new MapConfiguration(map);
		authMaterial.initialize(config);

		var builder = new Options.Builder();

		// act

		authMaterial.applyTo(builder);

		// assert

		var options = builder.build();
		assertThat(options).isNotNull();
	}

	@Test
	void shouldApplyUsernameAndPassword() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "username-and-password");
		map.put("username", "testuser");
		map.put("password", "testpass");
		var config = new MapConfiguration(map);
		authMaterial.initialize(config);

		var builder = new Options.Builder();

		// act

		authMaterial.applyTo(builder);

		// assert

		var options = builder.build();
		assertThat(options).isNotNull();
	}

	@Test
	void shouldApplyToken() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "token");
		map.put("token", "my-secret-token");
		var config = new MapConfiguration(map);
		authMaterial.initialize(config);

		var builder = new Options.Builder();

		// act

		authMaterial.applyTo(builder);

		// assert

		var options = builder.build();
		assertThat(options).isNotNull();
	}

	@Test
	void shouldApplyNKey() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "nkey");
		map.put("nkey-seed", "SUACSSL3UAHUDXKFSNVUZRF5UHPMWZ6BFDTJ7M6USDXIEDNPPQYYYCU3VY");
		var config = new MapConfiguration(map);
		authMaterial.initialize(config);

		var builder = new Options.Builder();

		// act

		authMaterial.applyTo(builder);

		// assert

		var options = builder.build();
		assertThat(options).isNotNull();
		assertThat(options.getAuthHandler()).isNotNull();
	}

	@Test
	void shouldApplyCredentialsFileText() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var credsContent = "-----BEGIN NATS USER JWT-----\neyJ0eXAiOiJKV1QiLCJhbGciOiJlZDI1NTE5LW5rZXkifQ.eyJqdGkiOiJBQkNERUYiLCJpYXQiOjE3MDQwNjcyMDAsImlzcyI6IkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsInN1YiI6IlVBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsIm5hdHMiOnt9fQ.SIGNATURE\n------END NATS USER JWT------\n-----BEGIN USER NKEY SEED-----\nSUACSSL3UAHUDXKFSNVUZRF5UHPMWZ6BFDTJ7M6USDXIEDNPPQYYYCU3VY\n------END USER NKEY SEED------";
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "credentials-file-text");
		map.put("credentials-file-text", credsContent);
		var config = new MapConfiguration(map);
		authMaterial.initialize(config);

		var builder = new Options.Builder();

		// act

		authMaterial.applyTo(builder);

		// assert

		var options = builder.build();
		assertThat(options).isNotNull();
		assertThat(options.getAuthHandler()).isNotNull();
	}

	@Test
	void shouldApplyCredentialsFileBase64() {

		// arrange

		var authMaterial = new NatsAuthMaterial();
		var credsContent = "-----BEGIN NATS USER JWT-----\neyJ0eXAiOiJKV1QiLCJhbGciOiJlZDI1NTE5LW5rZXkifQ.eyJqdGkiOiJBQkNERUYiLCJpYXQiOjE3MDQwNjcyMDAsImlzcyI6IkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsInN1YiI6IlVBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQSIsIm5hdHMiOnt9fQ.SIGNATURE\n------END NATS USER JWT------\n-----BEGIN USER NKEY SEED-----\nSUACSSL3UAHUDXKFSNVUZRF5UHPMWZ6BFDTJ7M6USDXIEDNPPQYYYCU3VY\n------END USER NKEY SEED------";
		var credsBase64 = Base64.getEncoder().encodeToString(credsContent.getBytes(StandardCharsets.UTF_8));
		var map = new HashMap<String, Object>();
		map.put("authentication-method", "credentials-file-base64");
		map.put("credentials-file-base64", credsBase64);
		var config = new MapConfiguration(map);
		authMaterial.initialize(config);

		var builder = new Options.Builder();

		// act

		authMaterial.applyTo(builder);

		// assert

		var options = builder.build();
		assertThat(options).isNotNull();
		assertThat(options.getAuthHandler()).isNotNull();
	}
}
