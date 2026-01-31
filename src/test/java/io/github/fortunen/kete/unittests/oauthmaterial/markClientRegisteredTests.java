package io.github.fortunen.kete.unittests.oauthmaterial;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.OAuthMaterial;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class markClientRegisteredTests {

	@Test
	void shouldSetClientRegisteredToTrue() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "test-realm");
		var config = new MapConfiguration(map);

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		assertThat(oauth.requiresClientRegistration())
			.as("Precondition: should require registration before marking")
			.isTrue();

		// act

		oauth.markClientRegistered();

		// assert

		assertThat(oauth.isClientRegistered())
			.as("Should set clientRegistered to true")
			.isTrue();
		assertThat(oauth.requiresClientRegistration())
			.as("Should no longer require registration after marking")
			.isFalse();
	}

	@Test
	void shouldBeIdempotent() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("mode", "internal");
		map.put("realm", "test-realm");
		var config = new MapConfiguration(map);

		var oauth = OAuthMaterial.builder()
			.withConfiguration(config)
			.build();

		// act

		oauth.markClientRegistered();
		oauth.markClientRegistered(); // Call again
		oauth.markClientRegistered(); // And again

		// assert

		assertThat(oauth.isClientRegistered())
			.as("Should remain true after multiple calls")
			.isTrue();
	}
}
