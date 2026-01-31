package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireInRangeElse_longTests {

	@Test
	public void shouldReturnValueWhenInRange() {

		// arrange

		var value = 5L;
		var min = 1L;
		var max = 10L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(5L);
	}

	@Test
	public void shouldReturnValueWhenEqualsMin() {

		// arrange

		var value = 1L;
		var min = 1L;
		var max = 10L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(1L);
	}

	@Test
	public void shouldReturnValueWhenEqualsMax() {

		// arrange

		var value = 10L;
		var min = 1L;
		var max = 10L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldReturnDefaultWhenBelowMin() {

		// arrange

		var value = 0L;
		var min = 1L;
		var max = 10L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(100L);
	}

	@Test
	public void shouldReturnDefaultWhenAboveMax() {

		// arrange

		var value = 11L;
		var min = 1L;
		var max = 10L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(100L);
	}

	@Test
	public void shouldHandleNegativeRange() {

		// arrange

		var value = -5L;
		var min = -10L;
		var max = -1L;
		var defaultValue = 0L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(-5L);
	}

	@Test
	public void shouldHandleRangeCrossingZero() {

		// arrange

		var value = 0L;
		var min = -10L;
		var max = 10L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(0L);
	}

	@Test
	public void shouldHandleSingleValueRange() {

		// arrange

		var value = 5L;
		var min = 5L;
		var max = 5L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(5L);
	}

	@Test
	public void shouldReturnDefaultForSingleValueRangeOutside() {

		// arrange

		var value = 6L;
		var min = 5L;
		var max = 5L;
		var defaultValue = 100L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(100L);
	}

	@Test
	public void shouldHandleNegativeDefault() {

		// arrange

		var value = 15L;
		var min = 1L;
		var max = 10L;
		var defaultValue = -99L;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(-99L);
	}

	@Test
	public void shouldHandleTimestampRange() {

		// arrange

		var now = System.currentTimeMillis();
		var oneYearAgo = now - (365L * 24 * 60 * 60 * 1000);
		var oneYearFromNow = now + (365L * 24 * 60 * 60 * 1000);
		var veryOld = 0L;
		var defaultTimestamp = now;

		// act

		var result1 = ValidationUtils.requireInRangeElse(now, oneYearAgo, oneYearFromNow, defaultTimestamp);
		var result2 = ValidationUtils.requireInRangeElse(veryOld, oneYearAgo, oneYearFromNow, defaultTimestamp);

		// assert

		assertThat(result1).isEqualTo(now);
		assertThat(result2).isEqualTo(defaultTimestamp);
	}
}
