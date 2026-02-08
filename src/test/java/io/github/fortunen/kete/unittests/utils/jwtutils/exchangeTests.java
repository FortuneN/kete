package io.github.fortunen.kete.unittests.utils.jwtutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.JwtUtils;
import org.junit.jupiter.api.Test;

public class exchangeTests {

	@Test
	public void shouldThrowWhenTokenRequestIsNull() {

		// act

		var thrown = catchThrowable(() -> {
			JwtUtils.exchange(null);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("tokenRequest is required");
	}
}
