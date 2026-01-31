package io.github.fortunen.kete.unittests.utils.templateutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.TemplateUtils;
import org.junit.jupiter.api.Test;

class clearCacheTests {

	@Test
	void shouldClearCacheWithoutError() {

		// act

		TemplateUtils.clearCache();

		// assert - no exception thrown

		assertThat(true).isTrue();
	}

	@Test
	void shouldAllowMultipleClearCacheCalls() {

		// act

		TemplateUtils.clearCache();
		TemplateUtils.clearCache();
		TemplateUtils.clearCache();

		// assert - no exception thrown

		assertThat(true).isTrue();
	}
}
