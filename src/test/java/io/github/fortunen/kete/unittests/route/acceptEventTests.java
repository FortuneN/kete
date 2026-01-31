package io.github.fortunen.kete.unittests.route;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.common.util.concurrent.UncheckedExecutionException;
import io.github.fortunen.kete.MatchMode;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.matchers.GlobMatcher;
import io.github.fortunen.kete.matchers.ListMatcher;
import org.junit.jupiter.api.Test;

class acceptEventTests extends TestBase {

	@Test
	void shouldReturnFalseWhenEventTypeIsNull() {

		// arrange

		route = new Route();

		// act

		var result = route.acceptEvent(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenEventTypeIsEmpty() {

		// arrange

		route = new Route();

		// act

		var result = route.acceptEvent("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenEventTypeIsBlank() {

		// arrange

		route = new Route();

		// act

		var result = route.acceptEvent("   ");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenNoEventMatchers() {

		// arrange

		route = new Route();
		route.setEventMatchers(new Matcher[0]);

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnTrueWhenEventMatchersIsNull() {

		// arrange

		route = new Route();
		route.setEventMatchers(null);

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnTrueWhenEventMatchesMatcher() {

		// arrange

		var matcher = createAndInitializeMatcher(new GlobMatcher(), "*");
		route = new Route();
		route.setEventMatchers(new Matcher[] { matcher });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenEventDoesNotMatchMatcher() {

		// arrange

		var matcher = createAndInitializeMatcher(new ListMatcher(), "LOGOUT,REGISTER");
		route = new Route();
		route.setEventMatchers(new Matcher[] { matcher });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenAnyMatcherAcceptsWithDefaultMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new GlobMatcher(), "*");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "LOGOUT,REGISTER");
		route = new Route();
		route.setEventMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnTrueWhenAllMatchersAcceptWithDefaultMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new GlobMatcher(), "LOG*");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "LOGIN,LOGOUT");
		route = new Route();
		route.setEventMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldCacheEventResults() {

		// arrange

		var matcher = createAndInitializeMatcher(new ListMatcher(), "LOGIN");
		route = new Route();
		route.setEventMatchers(new Matcher[] { matcher });

		// act

		var result1 = route.acceptEvent("LOGIN");
		var result2 = route.acceptEvent("LOGIN");

		// assert

		assertThat(result1).isTrue();
		assertThat(result2).isTrue();
	}

	@Test
	void shouldCacheDifferentEventTypes() {

		// arrange

		var matcher = createAndInitializeMatcher(new ListMatcher(), "LOGIN,LOGOUT");
		route = new Route();
		route.setEventMatchers(new Matcher[] { matcher });

		// act

		var resultLogin = route.acceptEvent("LOGIN");
		var resultLogout = route.acceptEvent("LOGOUT");
		var resultRegister = route.acceptEvent("REGISTER");

		// assert

		assertThat(resultLogin).isTrue();
		assertThat(resultLogout).isTrue();
		assertThat(resultRegister).isFalse();
	}

	@Test
	void shouldReturnTrueWhenAnyMatcherAcceptsInAnyMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new ListMatcher(), "LOGOUT");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "LOGIN");
		route = new Route();
		route.setEventMatchMode(MatchMode.ANY);
		route.setEventMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenNoMatcherAcceptsInAnyMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new ListMatcher(), "LOGOUT");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "REGISTER");
		route = new Route();
		route.setEventMatchMode(MatchMode.ANY);
		route.setEventMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldDefaultToDefaultMode() {

		// arrange

		route = new Route();

		// act & assert

		assertThat(route.getEventMatchMode()).isEqualTo(MatchMode.DEFAULT);
	}

	@Test
	void shouldReturnTrueWhenAllMatchersAcceptInAllMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new GlobMatcher(), "LOG*");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "LOGIN,LOGOUT");
		route = new Route();
		route.setEventMatchMode(MatchMode.ALL);
		route.setEventMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenAnyMatcherRejectsInAllMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new GlobMatcher(), "*");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "LOGOUT,REGISTER");
		route = new Route();
		route.setEventMatchMode(MatchMode.ALL);
		route.setEventMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptEvent("LOGIN");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldThrowExecutionExceptionViaLombok() {

		// arrange

		var throwingMatcher = new GlobMatcher() {
			@Override
			public boolean accept(String eventType) {
				throw new RuntimeException("Matcher error");
			}
		};

		route = new Route();
		route.setEventMatchers(new Matcher[] { throwingMatcher });

		// act

		var thrown = catchThrowable(() -> route.acceptEvent("ERROR_EVENT"));

		// assert

		assertThat(thrown)
			.as("Should throw UncheckedExecutionException via Lombok @SneakyThrows when matcher throws")
			.isInstanceOf(UncheckedExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);

		assertThat(thrown.getCause())
			.as("Inner exception should be the RuntimeException from the matcher")
			.hasMessage("Matcher error");
	}
}
