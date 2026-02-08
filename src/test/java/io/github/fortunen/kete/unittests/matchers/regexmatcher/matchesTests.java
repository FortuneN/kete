package io.github.fortunen.kete.unittests.matchers.regexmatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.matchers.RegexMatcher;
import org.junit.jupiter.api.Test;

class matchesTests {

	private RegexMatcher createAndInitialize(String pattern) {

		var matcher = new RegexMatcher();
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
	void shouldMatchWithDotStar() {

		// arrange

		var matcher = createAndInitialize("LOG.*");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("REGISTER")).isFalse();
	}

	@Test
	void shouldMatchWithAnchors() {

		// arrange

		var matcher = createAndInitialize("^LOGIN$");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGINX")).isFalse();
		assertThat(matcher.matches("XLOGIN")).isFalse();
	}

	@Test
	void shouldMatchWithCharacterClass() {

		// arrange

		var matcher = createAndInitialize("[A-Z]+_ERROR");

		// act & assert - RegexMatcher is case-insensitive

		assertThat(matcher.matches("LOGIN_ERROR")).isTrue();
		assertThat(matcher.matches("REGISTER_ERROR")).isTrue();
		assertThat(matcher.matches("login_ERROR")).isTrue(); // case-insensitive
	}

	@Test
	void shouldMatchWithAlternation() {

		// arrange

		var matcher = createAndInitialize("LOGIN|LOGOUT|REGISTER");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
		assertThat(matcher.matches("REGISTER")).isTrue();
		assertThat(matcher.matches("DELETE")).isFalse();
	}

	@Test
	void shouldMatchWithQuantifiers() {

		// arrange

		var matcher = createAndInitialize("LO+GIN");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOOGIN")).isTrue();
		assertThat(matcher.matches("LGIN")).isFalse();
	}

	@Test
	void shouldMatchWithOptionalCharacter() {

		// arrange

		var matcher = createAndInitialize("LOGS?IN");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGSIN")).isTrue();
		assertThat(matcher.matches("LOGSSIN")).isFalse();
	}

	@Test
	void shouldNotMatchEmptyString() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act & assert

		assertThat(matcher.matches("")).isFalse();
	}

	@Test
	void shouldMatchCaseInsensitiveWithPattern() {

		// arrange

		var matcher = createAndInitialize("(?i)login");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("login")).isTrue();
		assertThat(matcher.matches("Login")).isTrue();
	}

	@Test
	void shouldMatchWithWordBoundary() {

		// arrange

		var matcher = createAndInitialize("\\bLOGIN\\b");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGINX")).isFalse();
	}

	@Test
	void shouldMatchWithDigitClass() {

		// arrange

		var matcher = createAndInitialize("EVENT_\\d+");

		// act & assert

		assertThat(matcher.matches("EVENT_123")).isTrue();
		assertThat(matcher.matches("EVENT_1")).isTrue();
		assertThat(matcher.matches("EVENT_ABC")).isFalse();
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
