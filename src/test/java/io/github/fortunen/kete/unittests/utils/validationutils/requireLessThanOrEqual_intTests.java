package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireLessThanOrEqual_intTests {

	@Test
	public void shouldPassWhenValueLessThanMax() {

		// arrange

		var value = 5;
		var max = 10;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldPassWhenValueEqualsMax() {

		// arrange

		var value = 10;
		var max = 10;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldThrowWhenValueGreaterThanMax() {

		// arrange

		var value = 15;
		var max = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Value must be <= max");
	}

	@Test
	public void shouldPassWhenValueOneLessThanMax() {

		// arrange

		var value = 9;
		var max = 10;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(9);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -5;
		var max = -3;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(-5);
	}

	@Test
	public void shouldThrowWhenNegativeValueGreater() {

		// arrange

		var value = -3;
		var max = -5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be <= max");
	}

	@Test
	public void shouldPassWhenValueMinInt() {

		// arrange

		var value = Integer.MIN_VALUE;
		var max = Integer.MIN_VALUE;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(Integer.MIN_VALUE);
	}

	@Test
	public void shouldThrowWhenValueMaxInt() {

		// arrange

		var value = Integer.MAX_VALUE;
		var max = 0;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be <= max");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = 15;
		var max = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThanOrEqual(value, max, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = 5;
		var max = 10;
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireLessThanOrEqual(value, max, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleZeroBoundary() {

		// arrange

		var zero = 0;
		var one = 1;
		var minusOne = -1;

		// act

		var result1 = ValidationUtils.requireLessThanOrEqual(zero, zero, "Must be <= 0");
		var result2 = ValidationUtils.requireLessThanOrEqual(minusOne, zero, "Must be <= 0");

		// assert

		assertThat(result1).isEqualTo(0);
		assertThat(result2).isEqualTo(-1);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThanOrEqual(one, zero, "Must be <= 0");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be <= 0");
	}
}
