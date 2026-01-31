package io.github.fortunen.kete.unittests.route;

import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.Route;
import org.junit.jupiter.api.AfterEach;

public class TestBase {

	protected Route route;

	@AfterEach
	void cleanup() throws Exception {

		if (route != null) {
			route.close();
			route = null;
		}
	}

	protected Matcher createAndInitializeMatcher(Matcher matcher, String pattern) {

		matcher.setPattern(pattern);
		matcher.initialize();
		return matcher;
	}
}
