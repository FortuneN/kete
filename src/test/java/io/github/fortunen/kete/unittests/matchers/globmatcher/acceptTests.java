package io.github.fortunen.kete.unittests.matchers.globmatcher;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.matchers.GlobMatcher;
import org.junit.jupiter.api.Test;

class acceptTests {

	private GlobMatcher createAndInitialize(String pattern, boolean not) {

		var matcher = new GlobMatcher();
		matcher.setPattern(pattern);
		matcher.setNot(not);
		matcher.initialize();
		return matcher;
	}

	@Test
	void shouldAcceptMatchingEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN*", false);

		// act

		var result = matcher.accept("LOGIN_SUCCESS");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldRejectNonMatchingEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN*", false);

		// act

		var result = matcher.accept("LOGOUT");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldInvertMatchWhenNotIsTrue() {

		// arrange

		var matcher = createAndInitialize("LOGIN*", true);

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

		var matcher = createAndInitialize("LOGIN*", false);

		// act

		var result = matcher.accept(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldRejectBlankEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN*", false);

		// act

		var result = matcher.accept("   ");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldRejectEmptyEventType() {

		// arrange

		var matcher = createAndInitialize("LOGIN*", false);

		// act

		var result = matcher.accept("");

		// assert

		assertThat(result).isFalse();
	}
}
