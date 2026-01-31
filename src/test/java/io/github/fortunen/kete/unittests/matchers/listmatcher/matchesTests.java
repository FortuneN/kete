package io.github.fortunen.kete.unittests.matchers.listmatcher;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.matchers.ListMatcher;
import org.junit.jupiter.api.Test;

class matchesTests {

	private ListMatcher createAndInitialize(String pattern) {

		var matcher = new ListMatcher();
		matcher.setPattern(pattern);
		matcher.initialize();
		return matcher;
	}

	@Test
	void shouldMatchExactItem() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isTrue();
	}

	@Test
	void shouldNotMatchNonListedItem() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT");

		// act & assert

		assertThat(matcher.matches("REGISTER")).isFalse();
		assertThat(matcher.matches("LOGIN_ERROR")).isFalse();
	}

	@Test
	void shouldBeCaseInsensitive() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act & assert - all should match because ListMatcher is case-insensitive

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("login")).isTrue();
		assertThat(matcher.matches("Login")).isTrue();
	}

	@Test
	void shouldMatchSingleItem() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act & assert

		assertThat(matcher.matches("LOGIN")).isTrue();
		assertThat(matcher.matches("LOGOUT")).isFalse();
	}

	@Test
	void shouldNotMatchPartialItem() {

		// arrange

		var matcher = createAndInitialize("LOGIN");

		// act & assert

		assertThat(matcher.matches("LOG")).isFalse();
		assertThat(matcher.matches("LOGINS")).isFalse();
	}
}
