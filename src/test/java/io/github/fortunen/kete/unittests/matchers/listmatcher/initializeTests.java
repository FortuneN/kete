package io.github.fortunen.kete.unittests.matchers.listmatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.matchers.ListMatcher;
import org.junit.jupiter.api.Test;

class initializeTests {

	@Test
	void shouldThrowWhenPatternIsNull() {

		// arrange

		var matcher = new ListMatcher();

		// act & assert

		assertThatThrownBy(() -> matcher.initialize())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("pattern is required");
	}

	@Test
	void shouldParseCommaSeparatedList() {

		// arrange

		var matcher = new ListMatcher();
		matcher.setPattern("LOGIN,LOGOUT,REGISTER");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getEventTypes()).containsExactlyInAnyOrder("login", "logout", "register");
	}

	@Test
	void shouldTrimWhitespace() {

		// arrange

		var matcher = new ListMatcher();
		matcher.setPattern("  LOGIN  ,  LOGOUT  ,  REGISTER  ");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getEventTypes()).containsExactlyInAnyOrder("login", "logout", "register");
	}

	@Test
	void shouldFilterBlankItems() {

		// arrange

		var matcher = new ListMatcher();
		matcher.setPattern("LOGIN,,LOGOUT,  ,REGISTER");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getEventTypes()).containsExactlyInAnyOrder("login", "logout", "register");
	}

	@Test
	void shouldHandleSingleItem() {

		// arrange

		var matcher = new ListMatcher();
		matcher.setPattern("LOGIN");

		// act

		matcher.initialize();

		// assert

		assertThat(matcher.getEventTypes()).containsExactly("login");
	}
}
