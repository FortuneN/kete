package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requirePositiveElse_intTests {

	@Test
	public void shouldReturnValueWhenPositive() {

		// arrange

		var value = 42;
		var defaultValue = 10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(42);
	}

	@Test
	public void shouldReturnDefaultWhenZero() {

		// arrange

		var value = 0;
		var defaultValue = 10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldReturnDefaultWhenNegative() {

		// arrange

		var value = -5;
		var defaultValue = 10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldReturnValueWhenOne() {

		// arrange

		var value = 1;
		var defaultValue = 10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(1);
	}

	@Test
	public void shouldReturnDefaultWhenMinusOne() {

		// arrange

		var value = -1;
		var defaultValue = 10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldReturnValueWhenMaxInt() {

		// arrange

		var value = Integer.MAX_VALUE;
		var defaultValue = 10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void shouldReturnDefaultWhenMinInt() {

		// arrange

		var value = Integer.MIN_VALUE;
		var defaultValue = 10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldReturnNegativeDefault() {

		// arrange

		var value = 0;
		var defaultValue = -10;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(-10);
	}

	@Test
	public void shouldReturnZeroDefault() {

		// arrange

		var value = -5;
		var defaultValue = 0;

		// act

		var result = ValidationUtils.requirePositiveElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(0);
	}
}
