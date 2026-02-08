package io.github.fortunen.kete.unittests.utils.matcherutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.fortunen.kete.MatchMode;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.utils.MatcherUtils;
import org.junit.jupiter.api.Test;

class matchWithModeTests {

	// =========================================================================
	// ANY mode (default) - returns true if ANY matcher accepts
	// =========================================================================

	@Test
	void shouldReturnTrueWhenAnyMatcherAcceptsInAnyMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(false);
		when(matcher2.accept("test")).thenReturn(true);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ANY);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenNoMatcherAcceptsInAnyMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(false);
		when(matcher2.accept("test")).thenReturn(false);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ANY);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenFirstMatcherAcceptsInAnyMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(true);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ANY);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenMatchersArrayIsEmptyInAnyMode() {

		// arrange

		var matchers = new Matcher[0];

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ANY);

		// assert

		assertThat(result).isFalse();
	}

	// =========================================================================
	// ALL mode - returns true only if ALL matchers accept
	// =========================================================================

	@Test
	void shouldReturnTrueWhenAllMatchersAcceptInAllMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(true);
		when(matcher2.accept("test")).thenReturn(true);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ALL);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenAnyMatcherRejectsInAllMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(true);
		when(matcher2.accept("test")).thenReturn(false);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ALL);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenFirstMatcherRejectsInAllMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(false);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ALL);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenMatchersArrayIsEmptyInAllMode() {

		// arrange

		var matchers = new Matcher[0];

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ALL);

		// assert

		assertThat(result).isTrue();
	}

	// =========================================================================
	// DEFAULT mode - same as ANY
	// =========================================================================

	@Test
	void shouldReturnTrueWhenAnyMatcherAcceptsInDefaultMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(false);
		when(matcher2.accept("test")).thenReturn(true);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.DEFAULT);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenNoMatcherAcceptsInDefaultMode() {

		// arrange

		var matcher1 = mock(Matcher.class);
		var matcher2 = mock(Matcher.class);
		when(matcher1.accept("test")).thenReturn(false);
		when(matcher2.accept("test")).thenReturn(false);
		var matchers = new Matcher[] { matcher1, matcher2 };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.DEFAULT);

		// assert

		assertThat(result).isFalse();
	}

	// =========================================================================
	// Single matcher
	// =========================================================================

	@Test
	void shouldReturnTrueWhenSingleMatcherAccepts() {

		// arrange

		var matcher = mock(Matcher.class);
		when(matcher.accept("test")).thenReturn(true);
		var matchers = new Matcher[] { matcher };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ANY);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenSingleMatcherRejects() {

		// arrange

		var matcher = mock(Matcher.class);
		when(matcher.accept("test")).thenReturn(false);
		var matchers = new Matcher[] { matcher };

		// act

		var result = MatcherUtils.matchWithMode("test", matchers, MatchMode.ANY);

		// assert

		assertThat(result).isFalse();
	}

	// =========================================================================
	// Null inputs
	// =========================================================================

	@Test
	void shouldThrowForNullValue() {

		// act

		var thrown = catchThrowable(() -> MatcherUtils.matchWithMode(null, new Matcher[0], MatchMode.ANY));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("value is required");
	}

	@Test
	void shouldThrowForNullMatchers() {

		// act

		var thrown = catchThrowable(() -> MatcherUtils.matchWithMode("test", null, MatchMode.ANY));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("matchers is required");
	}

	@Test
	void shouldThrowForNullMode() {

		// act

		var thrown = catchThrowable(() -> MatcherUtils.matchWithMode("test", new Matcher[0], null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("mode is required");
	}
}
