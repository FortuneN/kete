package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonNegativeElse_longTests {

	@Test
	public void shouldReturnValueWhenZero() {

		// arrange

		var value = 0L;
		var defaultValue = 10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(0L);
	}

	@Test
	public void shouldReturnValueWhenPositive() {

		// arrange

		var value = 42L;
		var defaultValue = 10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(42L);
	}

	@Test
	public void shouldReturnDefaultWhenNegative() {

		// arrange

		var value = -5L;
		var defaultValue = 10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldReturnValueWhenOne() {

		// arrange

		var value = 1L;
		var defaultValue = 10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(1L);
	}

	@Test
	public void shouldReturnDefaultWhenMinusOne() {

		// arrange

		var value = -1L;
		var defaultValue = 10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldReturnValueWhenMaxLong() {

		// arrange

		var value = Long.MAX_VALUE;
		var defaultValue = 10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void shouldReturnDefaultWhenMinLong() {

		// arrange

		var value = Long.MIN_VALUE;
		var defaultValue = 10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldReturnNegativeDefault() {

		// arrange

		var value = -5L;
		var defaultValue = -10L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(-10L);
	}

	@Test
	public void shouldReturnZeroDefaultForNegativeValue() {

		// arrange

		var value = -5L;
		var defaultValue = 0L;

		// act

		var result = ValidationUtils.requireNonNegativeElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(0L);
	}

	@Test
	public void shouldHandleTimestamps() {

		// arrange

		var validTimestamp = System.currentTimeMillis();
		var invalidTimestamp = -1L;
		var defaultValue = 0L;

		// act

		var result1 = ValidationUtils.requireNonNegativeElse(validTimestamp, defaultValue);
		var result2 = ValidationUtils.requireNonNegativeElse(invalidTimestamp, defaultValue);

		// assert

		assertThat(result1).isEqualTo(validTimestamp);
		assertThat(result2).isEqualTo(0L);
	}
}
