package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonNegative_intTests {

	@Test
	public void shouldPassWhenValueZero() {

		// arrange

		var value = 0;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldPassWhenValuePositive() {

		// arrange

		var value = 42;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(42);
	}

	@Test
	public void shouldThrowWhenValueNegative() {

		// arrange

		var value = -1;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNegative(value, "Value must be non-negative");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Value must be non-negative");
	}

	@Test
	public void shouldThrowWhenValueMinValue() {

		// arrange

		var value = Integer.MIN_VALUE;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNegative(value, "Value must be non-negative");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be non-negative");
	}

	@Test
	public void shouldPassWhenValueMaxValue() {

		// arrange

		var value = Integer.MAX_VALUE;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void shouldPassWhenValueOne() {

		// arrange

		var value = 1;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(1);
	}

	@Test
	public void shouldThrowWhenValueNegativeOne() {

		// arrange

		var value = -1;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNegative(value, "Value must be non-negative");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be non-negative");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = -100;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNegative(value, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = 5;
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireNonNegative(value, () -> {
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
		var minusOne = -1;

		// act

		var resultZero = ValidationUtils.requireNonNegative(zero, "Must be non-negative");

		// assert

		assertThat(resultZero).isEqualTo(0);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNegative(minusOne, "Must be non-negative");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be non-negative");
	}
}
