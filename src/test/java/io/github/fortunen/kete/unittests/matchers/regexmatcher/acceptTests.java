package io.github.fortunen.kete.unittests.matchers.regexmatcher;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.matchers.RegexMatcher;
import org.junit.jupiter.api.Test;

class acceptTests {

	private RegexMatcher createAndInitialize(String pattern, boolean not) {

		var matcher = new RegexMatcher();
		matcher.setPattern(pattern);
		matcher.setNot(not);
		matcher.initialize();
		return matcher;
	}

	@Test
	void shouldAcceptMatchingEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN.*", false);

		// act

		var result = matcher.accept("LOGIN_SUCCESS");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldRejectNonMatchingEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN.*", false);

		// act

		var result = matcher.accept("LOGOUT");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldInvertMatchWhenNotIsTrue() {

		// arrange

		var matcher = createAndInitialize("LOGIN.*", true);

		// act

		var matchingResult = matcher.accept("LOGIN_SUCCESS");
		var nonMatchingResult = matcher.accept("LOGOUT");

		// assert

		assertThat(matchingResult).isFalse();
		assertThat(nonMatchingResult).isTrue();
	}

	@Test
	void shouldRejectNullEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN.*", false);

		// act

		var result = matcher.accept(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldRejectBlankEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN.*", false);

		// act

		var result = matcher.accept("   ");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldRejectEmptyEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN.*", false);

		// act

		var result = matcher.accept("");

		// assert

		assertThat(result).isFalse();
	}
}
