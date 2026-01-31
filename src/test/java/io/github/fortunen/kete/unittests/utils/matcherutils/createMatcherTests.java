package io.github.fortunen.kete.unittests.utils.matcherutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.matchers.GlobMatcher;
import io.github.fortunen.kete.matchers.ListMatcher;
import io.github.fortunen.kete.matchers.RegexMatcher;
import io.github.fortunen.kete.matchers.SqlMatcher;
import io.github.fortunen.kete.utils.MatcherUtils;
import org.junit.jupiter.api.Test;

class createMatcherTests {

	@Test
	void shouldThrowWhenNameIsNull() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher(null, "glob:*"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("name is required");
	}

	@Test
	void shouldThrowWhenNameIsEmpty() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("", "glob:*"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("name is required");
	}

	@Test
	void shouldThrowWhenNameIsBlank() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("   ", "glob:*"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("name is required");
	}

	@Test
	void shouldThrowWhenValueIsNull() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("test", null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("matcher value is required");
	}

	@Test
	void shouldThrowWhenValueIsEmpty() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("test", ""))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("matcher value is required");
	}

	@Test
	void shouldThrowWhenValueIsBlank() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("test", "   "))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("matcher value is required");
	}

	@Test
	void shouldThrowWhenFormatIsInvalid() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("test", "invalid"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("matcher must be in format 'kind:pattern' or 'kind:not:pattern'");
	}

	@Test
	void shouldThrowWhenKindIsEmpty() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("test", ":pattern"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("kind is required");
	}

	@Test
	void shouldThrowWhenPatternIsEmpty() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("test", "glob:"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("pattern is required");
	}

	@Test
	void shouldThrowWhenKindIsUnknown() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatcher("test", "unknown:pattern"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("matcher kind 'unknown' not found");
	}

	@Test
	void shouldCreateGlobMatcher() {

		// act

		var result = MatcherUtils.createMatcher("test-matcher", "glob:LOGIN*");

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(GlobMatcher.class);
		assertThat(result.getName()).isEqualTo("test-matcher");
		assertThat(result.getPattern()).isEqualTo("LOGIN*");
	}

	@Test
	void shouldCreateRegexMatcher() {

		// act

		var result = MatcherUtils.createMatcher("test-matcher", "regex:^LOGIN.*");

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(RegexMatcher.class);
		assertThat(result.getName()).isEqualTo("test-matcher");
		assertThat(result.getPattern()).isEqualTo("^LOGIN.*");
	}

	@Test
	void shouldCreateListMatcher() {

		// act

		var result = MatcherUtils.createMatcher("test-matcher", "list:LOGIN,LOGOUT,REGISTER");

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(ListMatcher.class);
		assertThat(result.getName()).isEqualTo("test-matcher");
		assertThat(result.getPattern()).isEqualTo("LOGIN,LOGOUT,REGISTER");
	}

	@Test
	void shouldCreateSqlMatcher() {

		// act

		var result = MatcherUtils.createMatcher("test-matcher", "sql:eventType LIKE 'LOGIN%'");

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(SqlMatcher.class);
		assertThat(result.getName()).isEqualTo("test-matcher");
		assertThat(result.getPattern()).isEqualTo("eventType LIKE 'LOGIN%'");
	}

	@Test
	void shouldTrimMatcherName() {

		// act

		var result = MatcherUtils.createMatcher("  test-matcher  ", "glob:*");

		// assert

		assertThat(result.getName()).isEqualTo("test-matcher");
	}

	@Test
	void shouldTrimKindAndPattern() {

		// act

		var result = MatcherUtils.createMatcher("test", "  glob  :  LOGIN*  ");

		// assert

		assertThat(result).isInstanceOf(GlobMatcher.class);
		assertThat(result.getPattern()).isEqualTo("LOGIN*");
	}

	@Test
	void shouldHandlePatternWithColons() {

		// act

		var result = MatcherUtils.createMatcher("test", "regex:^http://.*:8080$");

		// assert

		assertThat(result).isInstanceOf(RegexMatcher.class);
		assertThat(result.getPattern()).isEqualTo("^http://.*:8080$");
	}

	@Test
	void shouldCreateMatcherWithNotPrefix() {

		// act

		var result = MatcherUtils.createMatcher("test", "glob:not:LOGIN*");

		// assert

		assertThat(result).isInstanceOf(GlobMatcher.class);
		assertThat(result.getName()).isEqualTo("test");
		assertThat(result.getPattern()).isEqualTo("LOGIN*");
		assertThat(result.isNot()).isTrue();
	}

	@Test
	void shouldCreateListMatcherWithNotPrefix() {

		// act

		var result = MatcherUtils.createMatcher("test", "list:not:LOGIN,LOGOUT");

		// assert

		assertThat(result).isInstanceOf(ListMatcher.class);
		assertThat(result.getPattern()).isEqualTo("LOGIN,LOGOUT");
		assertThat(result.isNot()).isTrue();
	}

	@Test
	void shouldNotSetNotFlagWithoutPrefix() {

		// act

		var result = MatcherUtils.createMatcher("test", "glob:LOGIN*");

		// assert

		assertThat(result.isNot()).isFalse();
	}

	@Test
	void shouldHandleNotPrefixCaseInsensitive() {

		// act

		var result = MatcherUtils.createMatcher("test", "glob:NOT:ADMIN*");

		// assert

		assertThat(result.isNot()).isTrue();
		assertThat(result.getPattern()).isEqualTo("ADMIN*");
	}
}
