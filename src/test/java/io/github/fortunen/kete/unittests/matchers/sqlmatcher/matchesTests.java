package io.github.fortunen.kete.unittests.matchers.sqlmatcher;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.matchers.SqlMatcher;
import org.junit.jupiter.api.Test;

class matchesTests {

	private SqlMatcher createAndInitialize(String pattern) {

		var matcher = new SqlMatcher();
		matcher.setPattern(pattern);
		matcher.initialize();
		return matcher;
	}

	@Test
	void shouldMatchExactString() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isFalse();
	}

	@Test
	void shouldMatchWithPercentWildcard() {

		// arrange

		var matcher = createAndInitialize("LOG%");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("REGISTER")).isFalse();
	}

	@Test
	void shouldMatchWithTrailingPercentWildcard() {

		// arrange

		var matcher = createAndInitialize("%ERROR");

		// act & assert

		assertThat(matcher.matches("LOGIN_ERROR")).isTrue();
		assertThat(matcher.matches("REGISTER_ERROR")).isTrue();
		assertThat(matcher.matches("LOGIN")).isFalse();
	}

	@Test
	void shouldMatchWithMiddlePercentWildcard() {

		// arrange

		var matcher = createAndInitialize("LOGIN%ERROR");

		// act & assert

		assertThat(matcher.matches("LOGIN_ERROR")).isTrue();
		assertThat(matcher.matches("LOGINERROR")).isTrue();
		assertThat(matcher.matches("LOGOUT_ERROR")).isFalse();
	}

	@Test
	void shouldMatchWithUnderscoreSingleChar() {

		// arrange

		var matcher = createAndInitialize("LOGIN_");

		// act & assert

		assertThat(matcher.matches("LOGINX")).isTrue();
		assertThat(matcher.matches("LOGIN1")).isTrue();
		assertThat(matcher.matches("LOGIN")).isFalse();
		assertThat(matcher.matches("LOGINXX")).isFalse();
	}

	@Test
	void shouldMatchAll() {

		// arrange

		var matcher = createAndInitialize("%");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("")).isTrue();
	}

	@Test
	void shouldMatchCombinedPatterns() {

		// arrange
		// Pattern: LOG + exactly 2 chars (_) + zero or more (%)
		// LOGIN = LOG + IN (2 chars) matches
		// LOGOUT = LOG + OUT (3 chars) matches
		// LOG alone doesn't match (needs at least 2 more chars)

		var matcher = createAndInitialize("LOG__%");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("LOGI")).isFalse();  // only 1 char after LOG
		assertThat(matcher.matches("LOG")).isFalse();   // no chars after LOG
	}

	@Test
	void shouldBeCaseInsensitive() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act & assert - all should match because SqlMatcher is case-insensitive

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("login")).isTrue();
		assertThat(matcher.matches("Login")).isTrue();
	}

	@Test
	void shouldBeCaseInsensitiveWithPattern() {

		// arrange - lowercase pattern

		var matcher = createAndInitialize("log%");

		// act & assert - should match uppercase event types

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("login")).isTrue();
	}
}
