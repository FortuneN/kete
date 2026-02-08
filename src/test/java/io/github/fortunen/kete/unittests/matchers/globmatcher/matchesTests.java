package io.github.fortunen.kete.unittests.matchers.globmatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.matchers.GlobMatcher;

class matchesTests {

	private GlobMatcher createAndInitialize(String pattern) {

		var matcher = new GlobMatcher();
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
	void shouldMatchWithWildcard() {

		// arrange

		var matcher = createAndInitialize("LOG*");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("REGISTER")).isFalse();
	}

	@Test
	void shouldMatchWithTrailingWildcard() {

		// arrange

		var matcher = createAndInitialize("*_ERROR");

		// act & assert

		assertThat(matcher.matches("LOGIN_ERROR")).isTrue();
		assertThat(matcher.matches("REGISTER_ERROR")).isTrue();
		assertThat(matcher.matches("LOGIN")).isFalse();
	}

	@Test
	void shouldMatchWithMiddleWildcard() {

		// arrange

		var matcher = createAndInitialize("LOGIN*ERROR");

		// act & assert

		assertThat(matcher.matches("LOGIN_ERROR")).isTrue();
		assertThat(matcher.matches("LOGINERROR")).isTrue();
		assertThat(matcher.matches("LOGOUT_ERROR")).isFalse();
	}

	@Test
	void shouldMatchWithQuestionMark() {

		// arrange

		var matcher = createAndInitialize("LOGIN?");

		// act & assert

		assertThat(matcher.matches("LOGINX")).isTrue();
		assertThat(matcher.matches("LOGIN1")).isTrue();
		assertThat(matcher.matches("LOGIN")).isFalse();
		assertThat(matcher.matches("LOGINXX")).isFalse();
	}

	@Test
	void shouldMatchAll() {

		// arrange

		var matcher = createAndInitialize("*");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("")).isTrue();
	}

	@Test
	void shouldMatchUnderscoreAsLiteral() {

		// arrange

		var matcher = createAndInitialize("LOGIN_ERROR*");

		// act & assert

		assertThat(matcher.matches("LOGIN_ERROR")).isTrue();
		assertThat(matcher.matches("LOGIN_ERROR_DETAILS")).isTrue();
		assertThat(matcher.matches("LOGINERROR")).isFalse();
	}

	@Test
	void shouldBeCaseInsensitive() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act & assert - all should match because GlobMatcher is case-insensitive

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("login")).isTrue();
		assertThat(matcher.matches("Login")).isTrue();
	}

	@Test
	void shouldBeCaseInsensitiveWithPattern() {

		// arrange - lowercase pattern

		var matcher = createAndInitialize("log*");

		// act & assert - should match uppercase event types

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("login")).isTrue();
	}

	@Test
	void shouldThrowForNullEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act

		var thrown = catchThrowable(() -> matcher.matches(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("eventType is required");
	}
}
