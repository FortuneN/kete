package io.github.fortunen.kete.unittests.utils.azureutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.azure.core.credential.TokenCredential;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AzureUtils;

class configureAuthenticationTests {

	@Test
	void shouldCallPrimaryAuthWhenTypeMatchesPrimaryAuthenticationType() {

		// arrange

		var primaryCalled = new AtomicBoolean(false);
		var config = new MapConfiguration(new HashMap<>());

		// act

		AzureUtils.configureAuthentication("connection-string", config, "connection-string", () -> primaryCalled.set(true), credential -> {});

		// assert

		assertThat(primaryCalled.get()).isTrue();
	}

	@Test
	void shouldNotCallOnTokenCredentialWhenTypeMatchesPrimaryAuthenticationType() {

		// arrange

		var tokenCredentialCalled = new AtomicBoolean(false);
		var config = new MapConfiguration(new HashMap<>());

		// act

		AzureUtils.configureAuthentication("connection-string", config, "connection-string", () -> {}, credential -> tokenCredentialCalled.set(true));

		// assert

		assertThat(tokenCredentialCalled.get()).isFalse();
	}

	@Test
	void shouldCallOnTokenCredentialWithManagedIdentityCredentialWhenTypeIsManagedIdentity() {

		// arrange

		var receivedCredential = new AtomicReference<TokenCredential>();
		var config = new MapConfiguration(new HashMap<>());

		// act

		AzureUtils.configureAuthentication("managed-identity", config, "connection-string", () -> {}, receivedCredential::set);

		// assert

		assertThat(receivedCredential.get()).isNotNull();
	}

	@Test
	void shouldNotCallPrimaryAuthWhenTypeIsManagedIdentity() {

		// arrange

		var primaryCalled = new AtomicBoolean(false);
		var config = new MapConfiguration(new HashMap<>());

		// act

		AzureUtils.configureAuthentication("managed-identity", config, "connection-string", () -> primaryCalled.set(true), credential -> {});

		// assert

		assertThat(primaryCalled.get()).isFalse();
	}

	@Test
	void shouldCallOnTokenCredentialWithDefaultAzureCredentialWhenTypeIsDefaultAzureCredential() {

		// arrange

		var receivedCredential = new AtomicReference<TokenCredential>();
		var config = new MapConfiguration(new HashMap<>());

		// act

		AzureUtils.configureAuthentication("default-azure-credential", config, "connection-string", () -> {}, receivedCredential::set);

		// assert

		assertThat(receivedCredential.get()).isNotNull();
	}

	@Test
	void shouldNotCallPrimaryAuthWhenTypeIsDefaultAzureCredential() {

		// arrange

		var primaryCalled = new AtomicBoolean(false);
		var config = new MapConfiguration(new HashMap<>());

		// act

		AzureUtils.configureAuthentication("default-azure-credential", config, "connection-string", () -> primaryCalled.set(true), credential -> {});

		// assert

		assertThat(primaryCalled.get()).isFalse();
	}

	@Test
	void shouldThrowWhenAuthenticationTypeIsUnsupported() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var thrown = catchThrowable(() -> AzureUtils.configureAuthentication("unknown-type", config, "connection-string", () -> {}, credential -> {}));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("unsupported authentication-type: 'unknown-type' — valid options: connection-string, managed-identity, default-azure-credential");
	}

	@Test
	void shouldIncludePrimaryAuthenticationTypeInErrorMessageWhenUnsupported() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var thrown = catchThrowable(() -> AzureUtils.configureAuthentication("invalid", config, "sas-token", () -> {}, credential -> {}));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("unsupported authentication-type: 'invalid' — valid options: sas-token, managed-identity, default-azure-credential");
	}

	@Test
	void shouldWorkWithDifferentPrimaryAuthenticationTypes() {

		// arrange

		var primaryCalled = new AtomicBoolean(false);
		var config = new MapConfiguration(new HashMap<>());

		// act

		AzureUtils.configureAuthentication("sas-token", config, "sas-token", () -> primaryCalled.set(true), credential -> {});

		// assert

		assertThat(primaryCalled.get()).isTrue();
	}

	@Test
	void shouldPassConfigurationToManagedIdentityCredentialCreation() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("managed-identity-client-id", "test-client-id");
		var config = new MapConfiguration(map);
		var receivedCredential = new AtomicReference<TokenCredential>();

		// act

		AzureUtils.configureAuthentication("managed-identity", config, "connection-string", () -> {}, receivedCredential::set);

		// assert

		assertThat(receivedCredential.get()).isNotNull();
	}
}
