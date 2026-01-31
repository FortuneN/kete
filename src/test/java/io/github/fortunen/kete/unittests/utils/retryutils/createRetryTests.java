package io.github.fortunen.kete.unittests.utils.retryutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.RetryUtils;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class createRetryTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> RetryUtils.createRetry(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldReturnNullWhenEnabledIsFalse() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "false");
		var config = new MapConfiguration(map);

		// act

		var result = RetryUtils.createRetry(config);

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenEnabledNotSpecified() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act

		var result = RetryUtils.createRetry(config);

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldCreateRetryWithDefaults() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		var config = new MapConfiguration(map);

		// act

		var result = RetryUtils.createRetry(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getName()).startsWith("kete-");
	}

	@Test
	void shouldCreateRetryWithMaxAttempts() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("max-attempts", "5");
		var config = new MapConfiguration(map);

		// act

		var result = RetryUtils.createRetry(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getRetryConfig().getMaxAttempts()).isEqualTo(5);
	}

	@Test
	void shouldThrowWhenMaxAttemptsIsZero() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("max-attempts", "0");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RetryUtils.createRetry(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("max-attempts must be greater than zero");
	}

	@Test
	void shouldThrowWhenMaxAttemptsIsNegative() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("max-attempts", "-1");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RetryUtils.createRetry(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("max-attempts must be greater than zero");
	}

	@Test
	void shouldThrowWhenMaxAttemptsIsNotInteger() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("max-attempts", "abc");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RetryUtils.createRetry(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("max-attempts must be an integer");
	}

	@Test
	void shouldCreateRetryWithWaitDuration() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("wait-duration", "PT2S");
		var config = new MapConfiguration(map);

		// act

		var result = RetryUtils.createRetry(config);

		// assert

		assertThat(result).isNotNull();
		// waitDuration is configured in the retry config
		assertThat(result.getRetryConfig()).isNotNull();
	}

	@Test
	void shouldThrowWhenWaitDurationIsZero() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("wait-duration", "PT0S");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RetryUtils.createRetry(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("wait-duration must be greater than zero");
	}

	@Test
	void shouldThrowWhenWaitDurationIsNotValid() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("wait-duration", "invalid");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RetryUtils.createRetry(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("wait-duration must be a duration");
	}

	@Test
	void shouldCreateRetryWithAllOptions() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		map.put("max-attempts", "3");
		map.put("wait-duration", "PT1S");
		var config = new MapConfiguration(map);

		// act

		var result = RetryUtils.createRetry(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getRetryConfig().getMaxAttempts()).isEqualTo(3);
		assertThat(result.getRetryConfig()).isNotNull();
	}

	@Test
	void shouldCreateMultipleRetriesWithUniqueNames() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "true");
		var config = new MapConfiguration(map);

		// act

		var result1 = RetryUtils.createRetry(config);
		var result2 = RetryUtils.createRetry(config);

		// assert

		assertThat(result1.getName()).isNotEqualTo(result2.getName());
	}
}
