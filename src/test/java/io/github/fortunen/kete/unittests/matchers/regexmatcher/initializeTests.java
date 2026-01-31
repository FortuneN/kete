package io.github.fortunen.kete.unittests.matchers.regexmatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.matchers.RegexMatcher;
import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.Test;

class initializeTests {

	@Test
	void shouldThrowWhenPatternIsNull() {

		// arrange

		var matcher = new RegexMatcher();

		// act & assert

		assertThatThrownBy(() -> matcher.initialize())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("pattern is required");
	}

	@Test
	void shouldCompilePatternSuccessfully() {

		// arrange

		var matcher = new RegexMatcher();
		matcher.setPattern("LOGIN.*");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getRegex()).isNotNull();
	}

	@Test
	void shouldCompileComplexRegexPattern() {

		// arrange

		var matcher = new RegexMatcher();
		matcher.setPattern("^(LOGIN|LOGOUT|REGISTER)_[A-Z]+$");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getRegex()).isNotNull();
		assertThat(matcher.getRegex().pattern()).isEqualTo("^(LOGIN|LOGOUT|REGISTER)_[A-Z]+$");
	}

	@Test
	void shouldThrowWhenPatternIsInvalidRegex() {

		// arrange

		var matcher = new RegexMatcher();
		matcher.setPattern("[invalid(regex");

		// act & assert

		assertThatThrownBy(() -> matcher.initialize())
			.isInstanceOf(PatternSyntaxException.class);
	}
}
