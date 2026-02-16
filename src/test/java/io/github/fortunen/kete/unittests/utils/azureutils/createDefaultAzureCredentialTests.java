package io.github.fortunen.kete.unittests.utils.azureutils;

import static org.assertj.core.api.Assertions.assertThat;

import com.azure.core.credential.TokenCredential;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AzureUtils;

class createDefaultAzureCredentialTests {

	@Test
	void shouldReturnTokenCredential() {

		// act

		var credential = AzureUtils.createDefaultAzureCredential();

		// assert

		assertThat(credential).isNotNull();
		assertThat(credential).isInstanceOf(TokenCredential.class);
	}
}
