package io.github.fortunen.kete.unittests.utils.matcherutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.matchers.GlobMatcher;
import io.github.fortunen.kete.utils.MatcherUtils;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class createMatchersTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> MatcherUtils.createMatchers(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldReturnEmptyArrayForEmptyConfiguration() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var result = MatcherUtils.createMatchers(config);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	void shouldCreateSingleMatcher() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("m1", "glob:LOGIN*");
		var config = new MapConfiguration(map);

		// act

		var result = MatcherUtils.createMatchers(config);

		// assert

		assertThat(result).hasSize(1);
		assertThat(result[0]).isInstanceOf(GlobMatcher.class);
		assertThat(result[0].getName()).isEqualTo("m1");
		assertThat(result[0].getPattern()).isEqualTo("LOGIN*");
	}

	@Test
	void shouldCreateMultipleMatchers() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("m1", "glob:LOGIN*");
		map.put("m2", "regex:^LOGOUT.*");
		map.put("m3", "list:REGISTER,UPDATE");
		var config = new MapConfiguration(map);

		// act

		var result = MatcherUtils.createMatchers(config);

		// assert

		assertThat(result).hasSize(3);
	}

	@Test
	void shouldCreateMixedMatcherTypes() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("globMatch", "glob:*.txt");
		map.put("regexMatch", "regex:\\d+");
		map.put("listMatch", "list:a,b,c");
		var config = new MapConfiguration(map);

		// act

		var result = MatcherUtils.createMatchers(config);

		// assert

		assertThat(result).hasSize(3);
		var names = Arrays.stream(result).map(m -> m.getName()).toList();
		assertThat(names).containsExactlyInAnyOrder("globMatch", "regexMatch", "listMatch");
	}

	@Test
	void shouldPreserveMatcherNames() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("myCustomName", "glob:*");
		var config = new MapConfiguration(map);

		// act

		var result = MatcherUtils.createMatchers(config);

		// assert

		assertThat(result[0].getName()).isEqualTo("myCustomName");
	}

	@Test
	void shouldHandlePatternWithColons() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("url", "regex:^http://localhost:8080$");
		var config = new MapConfiguration(map);

		// act

		var result = MatcherUtils.createMatchers(config);

		// assert

		assertThat(result).hasSize(1);
		assertThat(result[0].getPattern()).isEqualTo("^http://localhost:8080$");
	}
}
