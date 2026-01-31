package io.github.fortunen.kete.unittests.matchers.listmatcher;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.matchers.ListMatcher;
import org.junit.jupiter.api.Test;

class acceptTests {

	private ListMatcher createAndInitialize(String pattern, boolean not) {

		var matcher = new ListMatcher();
		matcher.setPattern(pattern);
		matcher.setNot(not);
		matcher.initialize();
		return matcher;
	}

	@Test
	void shouldAcceptMatchingEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT,REGISTER", false);

		// act

		var result = matcher.accept("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldRejectNonMatchingEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT,REGISTER", false);

		// act

		var result = matcher.accept("DELETE");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldInvertMatchWhenNotIsTrue() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT", true);

		// act

		var matchingResult = matcher.accept("LOGIN");
		var nonMatchingResult = matcher.accept("REGISTER");

		// assert

		assertThat(matchingResult).isFalse();
		assertThat(nonMatchingResult).isTrue();
	}

	@Test
	void shouldRejectNullEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT", false);

		// act

		var result = matcher.accept(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldRejectBlankEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT", false);

		// act

		var result = matcher.accept("   ");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldRejectEmptyEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN,LOGOUT", false);

		// act

		var result = matcher.accept("");

		// assert

		assertThat(result).isFalse();
	}
}
