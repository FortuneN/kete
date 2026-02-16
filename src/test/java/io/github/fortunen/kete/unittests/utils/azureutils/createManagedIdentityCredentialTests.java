package io.github.fortunen.kete.unittests.utils.azureutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.azure.core.credential.TokenCredential;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AzureUtils;

class createManagedIdentityCredentialTests {

	@Test
	void shouldReturnTokenCredential() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var credential = AzureUtils.createManagedIdentityCredential(config);

		// assert

		assertThat(credential).isNotNull();
		assertThat(credential).isInstanceOf(TokenCredential.class);
	}

	@Test
	void shouldReturnTokenCredentialWhenClientIdIsProvided() {

		// arrange

		var config = new MapConfiguration(Map.of("managed-identity-client-id", "test-client-id-123"));

		// act

		var credential = AzureUtils.createManagedIdentityCredential(config);

		// assert

		assertThat(credential).isNotNull();
	}

	@Test
	void shouldReturnTokenCredentialWhenClientIdIsBlank() {

		// arrange

		var config = new MapConfiguration(Map.of("managed-identity-client-id", "   "));

		// act

		var credential = AzureUtils.createManagedIdentityCredential(config);

		// assert

		assertThat(credential).isNotNull();
	}

	@Test
	void shouldReturnTokenCredentialWhenClientIdIsEmpty() {

		// arrange

		var config = new MapConfiguration(Map.of("managed-identity-client-id", ""));

		// act

		var credential = AzureUtils.createManagedIdentityCredential(config);

		// assert

		assertThat(credential).isNotNull();
	}

	@Test
	void shouldReturnTokenCredentialWhenClientIdIsMissing() {

		// arrange

		var config = new MapConfiguration(Map.of("some-other-key", "value"));

		// act

		var credential = AzureUtils.createManagedIdentityCredential(config);

		// assert

		assertThat(credential).isNotNull();
	}
}
