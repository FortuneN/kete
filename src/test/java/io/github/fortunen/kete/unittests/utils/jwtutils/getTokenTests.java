package io.github.fortunen.kete.unittests.utils.jwtutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.JwtUtils;
import org.junit.jupiter.api.Test;

public class getTokenTests {

	@Test
	public void shouldThrowWhenTokenRequestFactoryIsNull() {

		// arrange

		var holder = new JwtUtils.CachedTokenHolder();

		// act

		var thrown = catchThrowable(() -> {
			holder.getToken(null);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("tokenRequestFactory is required");
	}

	@Test
	public void shouldCreateNewInstanceSuccessfully() {

		// act

		var holder = new JwtUtils.CachedTokenHolder();

		// assert

		assertThat(holder).isNotNull();
	}
}
