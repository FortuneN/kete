package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireGreaterThanOrEqual_intTests {

	@Test
	public void shouldPassWhenValueGreaterThanMin() {

		// arrange

		var value = 10;
		var min = 5;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldPassWhenValueEqualsMin() {

		// arrange

		var value = 5;
		var min = 5;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldThrowWhenValueLessThanMin() {

		// arrange

		var value = 3;
		var min = 5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Value must be >= min");
	}

	@Test
	public void shouldPassWhenValueOneMoreThanMin() {

		// arrange

		var value = 6;
		var min = 5;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(6);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -3;
		var min = -5;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(-3);
	}

	@Test
	public void shouldThrowWhenNegativeValueLess() {

		// arrange

		var value = -5;
		var min = -3;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be >= min");
	}

	@Test
	public void shouldPassWhenValueMaxInt() {

		// arrange

		var value = Integer.MAX_VALUE;
		var min = Integer.MAX_VALUE;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void shouldThrowWhenValueMinInt() {

		// arrange

		var value = Integer.MIN_VALUE;
		var min = 0;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be >= min");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = 3;
		var min = 5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThanOrEqual(value, min, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = 10;
		var min = 5;
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireGreaterThanOrEqual(value, min, () -> {
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

		var result1 = ValidationUtils.requireGreaterThanOrEqual(zero, zero, "Must be >= 0");
		var result2 = ValidationUtils.requireGreaterThanOrEqual(one, zero, "Must be >= 0");

		// assert

		assertThat(result1).isEqualTo(0);
		assertThat(result2).isEqualTo(1);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThanOrEqual(minusOne, zero, "Must be >= 0");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be >= 0");
	}
}
