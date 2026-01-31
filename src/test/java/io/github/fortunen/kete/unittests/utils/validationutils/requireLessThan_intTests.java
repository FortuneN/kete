package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireLessThan_intTests {

	@Test
	public void shouldPassWhenValueLessThanMax() {

		// arrange

		var value = 5;
		var max = 10;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldThrowWhenValueEqualsMax() {

		// arrange

		var value = 10;
		var max = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThan(value, max, "Value must be less than max");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Value must be less than max");
	}

	@Test
	public void shouldThrowWhenValueGreaterThanMax() {

		// arrange

		var value = 15;
		var max = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThan(value, max, "Value must be less than max");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be less than max");
	}

	@Test
	public void shouldPassWhenValueOneLessThanMax() {

		// arrange

		var value = 9;
		var max = 10;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

		// assert

		assertThat(result).isEqualTo(9);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -5;
		var max = -3;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

		// assert

		assertThat(result).isEqualTo(-5);
	}

	@Test
	public void shouldThrowWhenNegativeValueNotLess() {

		// arrange

		var value = -3;
		var max = -5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThan(value, max, "Value must be less than max");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be less than max");
	}

	@Test
	public void shouldPassWhenValueMinInt() {

		// arrange

		var value = Integer.MIN_VALUE;
		var max = Integer.MIN_VALUE + 1;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

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
			ValidationUtils.requireLessThan(value, max, "Value must be less than max");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be less than max");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = 15;
		var max = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThan(value, max, () -> new IllegalStateException("Custom error"));
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

		ValidationUtils.requireLessThan(value, max, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleZeroBoundary() {

		// arrange

		var negativeValue = -1;
		var zero = 0;
		var positiveValue = 1;

		// act

		var result = ValidationUtils.requireLessThan(negativeValue, positiveValue, "Must be less");

		// assert

		assertThat(result).isEqualTo(-1);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThan(zero, zero, "Must be less");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be less");
		var thrown2 = catchThrowable(() -> {
			ValidationUtils.requireLessThan(positiveValue, zero, "Must be less");
		});
		assertThat(thrown2)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be less");
	}
}
