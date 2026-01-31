package io.github.fortunen.kete.unittests.route;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.MatchMode;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.Route;
import io.github.fortunen.kete.matchers.GlobMatcher;
import io.github.fortunen.kete.matchers.ListMatcher;
import org.junit.jupiter.api.Test;

class acceptRealmTests extends TestBase {

	@Test
	void shouldReturnFalseWhenRealmIsNull() {

		// arrange

		route = new Route();

		// act

		var result = route.acceptRealm(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenRealmIsEmpty() {

		// arrange

		route = new Route();

		// act

		var result = route.acceptRealm("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenRealmIsBlank() {

		// arrange

		route = new Route();

		// act

		var result = route.acceptRealm("   ");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenNoRealmMatchers() {

		// arrange

		route = new Route();
		route.setRealmMatchers(new Matcher[0]);

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnTrueWhenRealmMatchersIsNull() {

		// arrange

		route = new Route();
		route.setRealmMatchers(null);

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnTrueWhenRealmMatchesMatcher() {

		// arrange

		var matcher = createAndInitializeMatcher(new GlobMatcher(), "*");
		route = new Route();
		route.setRealmMatchers(new Matcher[] { matcher });

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenRealmDoesNotMatchMatcher() {

		// arrange

		var matcher = createAndInitializeMatcher(new ListMatcher(), "prod,staging");
		route = new Route();
		route.setRealmMatchers(new Matcher[] { matcher });

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenAnyMatcherAcceptsInAnyMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new ListMatcher(), "prod");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "master");
		route = new Route();
		route.setRealmMatchMode(MatchMode.ANY);
		route.setRealmMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenNoMatcherAcceptsInAnyMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new ListMatcher(), "prod");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "staging");
		route = new Route();
		route.setRealmMatchMode(MatchMode.ANY);
		route.setRealmMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenAllMatchersAcceptInAllMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new GlobMatcher(), "mas*");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "master,main");
		route = new Route();
		route.setRealmMatchMode(MatchMode.ALL);
		route.setRealmMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenAnyMatcherRejectsInAllMode() {

		// arrange

		var matcher1 = createAndInitializeMatcher(new GlobMatcher(), "*");
		var matcher2 = createAndInitializeMatcher(new ListMatcher(), "prod,staging");
		route = new Route();
		route.setRealmMatchMode(MatchMode.ALL);
		route.setRealmMatchers(new Matcher[] { matcher1, matcher2 });

		// act

		var result = route.acceptRealm("master");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	void shouldCacheRealmResults() {

		// arrange

		var matcher = createAndInitializeMatcher(new ListMatcher(), "master");
		route = new Route();
		route.setRealmMatchers(new Matcher[] { matcher });

		// act

		var result1 = route.acceptRealm("master");
		var result2 = route.acceptRealm("master");

		// assert

		assertThat(result1).isTrue();
		assertThat(result2).isTrue();
	}
}
