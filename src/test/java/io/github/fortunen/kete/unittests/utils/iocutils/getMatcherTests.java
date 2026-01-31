package io.github.fortunen.kete.unittests.utils.iocutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.matchers.GlobMatcher;
import io.github.fortunen.kete.matchers.ListMatcher;
import io.github.fortunen.kete.matchers.RegexMatcher;
import io.github.fortunen.kete.matchers.SqlMatcher;
import io.github.fortunen.kete.utils.IocUtils;
import org.junit.jupiter.api.Test;

public class getMatcherTests {

	@Test
	public void shouldGetGlobMatcher() {

		// act

		var result = IocUtils.get("glob", Matcher.class);

		// assert

		assertThat(result)
			.as("Should return a glob matcher")
			.isNotNull()
			.isInstanceOf(GlobMatcher.class);
	}

	@Test
	public void shouldGetRegexMatcher() {

		// act

		var result = IocUtils.get("regex", Matcher.class);

		// assert

		assertThat(result)
			.as("Should return a regex matcher")
			.isNotNull()
			.isInstanceOf(RegexMatcher.class);
	}

	@Test
	public void shouldGetListMatcher() {

		// act

		var result = IocUtils.get("list", Matcher.class);

		// assert

		assertThat(result)
			.as("Should return a list matcher")
			.isNotNull()
			.isInstanceOf(ListMatcher.class);
	}

	@Test
	public void shouldGetSqlMatcher() {

		// act

		var result = IocUtils.get("sql", Matcher.class);

		// assert

		assertThat(result)
			.as("Should return a SQL matcher")
			.isNotNull()
			.isInstanceOf(SqlMatcher.class);
	}

	@Test
	public void shouldReturnNewInstanceEachTimeForTransientMatchers() {

		// act

		var first = IocUtils.get("glob", Matcher.class);
		var second = IocUtils.get("glob", Matcher.class);

		// assert

		assertThat(first)
			.as("Should return different instances for transient matchers")
			.isNotSameAs(second);
	}
}
