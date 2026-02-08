package io.github.fortunen.kete.unittests.matchers.globmatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.matchers.GlobMatcher;
import org.junit.jupiter.api.Test;

class initializeTests {

	@Test
	void shouldThrowWhenPatternIsNull() {

		// arrange

		var matcher = new GlobMatcher();

		// act & assert

		assertThatThrownBy(() -> matcher.initialize())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("pattern is required");
	}

	@Test
	void shouldCompilePatternSuccessfully() {

		// arrange

		var matcher = new GlobMatcher();
		matcher.setPattern("LOGIN*");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getMatcher()).isNotNull();
	}

	@Test
	void shouldCompileEmptyPattern() {

		// arrange

		var matcher = new GlobMatcher();
		matcher.setPattern("");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getMatcher()).isNotNull();
	}
}
