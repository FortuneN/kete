package io.github.fortunen.kete.unittests.oauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.OAuthMaterial.OAuthMode;
import java.net.URI;
import org.junit.jupiter.api.Test;

class isInternalModeTests {

	@Test
	void shouldReturnTrueWhenModeIsInternal() {

		// arrange

		var oauth = OAuthMaterial.builder()
			.withEnabled(true)
			.withMode(OAuthMode.INTERNAL)
			.withTokenUri(URI.create("http://localhost/token"))
			.withClientId("test-client")
			.withClientSecret("test-secret")
			.withRealm("test-realm")
			.build();

		// act

		var result = oauth.isInternalMode();

		// assert

		assertThat(result)
			.as("Should return true when mode is INTERNAL")
			.isTrue();
	}

	@Test
	void shouldReturnFalseWhenModeIsExternal() {

		// arrange

		var oauth = OAuthMaterial.builder()
			.withEnabled(true)
			.withMode(OAuthMode.EXTERNAL)
			.withTokenUri(URI.create("http://localhost/token"))
			.withClientId("test-client")
			.withClientSecret("test-secret")
			.build();

		// act

		var result = oauth.isInternalMode();

		// assert

		assertThat(result)
			.as("Should return false when mode is EXTERNAL")
			.isFalse();
	}

	@Test
	void shouldReturnFalseWhenModeIsNull() {

		// arrange

		var oauth = OAuthMaterial.builder()
			.withEnabled(false)
			.build();

		// act

		var result = oauth.isInternalMode();

		// assert

		assertThat(result)
			.as("Should return false when mode is null")
			.isFalse();
	}
}
