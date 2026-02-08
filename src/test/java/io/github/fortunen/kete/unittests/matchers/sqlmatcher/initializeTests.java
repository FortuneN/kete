package io.github.fortunen.kete.unittests.matchers.sqlmatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.matchers.SqlMatcher;
import org.junit.jupiter.api.Test;

class initializeTests {

	@Test
	void shouldThrowWhenPatternIsNull() {

		// arrange

		var matcher = new SqlMatcher();

		// act & assert

		assertThatThrownBy(() -> matcher.initialize())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("pattern is required");
	}

	@Test
	void shouldCompilePatternSuccessfully() {

		// arrange

		var matcher = new SqlMatcher();
		matcher.setPattern("LOGIN%");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getMatcher()).isNotNull();
	}

	@Test
	void shouldCompileEmptyPattern() {

		// arrange

		var matcher = new SqlMatcher();
		matcher.setPattern("");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getMatcher()).isNotNull();
	}
}
