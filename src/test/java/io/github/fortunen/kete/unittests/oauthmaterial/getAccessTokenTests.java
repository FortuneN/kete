package io.github.fortunen.kete.unittests.oauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.OAuthMaterial.OAuthMode;
import java.net.URI;
import org.junit.jupiter.api.Test;

class getAccessTokenTests {

	@Test
	void shouldReturnNullWhenNotEnabled() {

		// arrange

		var oauth = OAuthMaterial.builder()
			.withEnabled(false)
			.build();

		// act

		var result = oauth.getAccessToken();

		// assert

		assertThat(result)
			.as("Should return null when OAuth is not enabled")
			.isNull();
	}

	@Test
	void shouldReturnNullWhenDisabledViaConfiguration() {

		// arrange

		var oauth = OAuthMaterial.builder()
			.withEnabled(false)
			.withMode(OAuthMode.EXTERNAL)
			.withTokenUri(URI.create("http://localhost/token"))
			.withClientId("test-client")
			.withClientSecret("test-secret")
			.build();

		// act

		var result = oauth.getAccessToken();

		// assert

		assertThat(result)
			.as("Should return null when OAuth is disabled even with full configuration")
			.isNull();
	}
}
