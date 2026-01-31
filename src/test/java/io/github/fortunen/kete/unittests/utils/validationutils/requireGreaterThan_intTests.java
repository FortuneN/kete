package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireGreaterThan_intTests {

	@Test
	public void shouldPassWhenValueGreaterThanMin() {

		// arrange

		var value = 10;
		var min = 5;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldThrowWhenValueEqualsMin() {

		// arrange

		var value = 5;
		var min = 5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Value must be greater than min");
	}

	@Test
	public void shouldThrowWhenValueLessThanMin() {

		// arrange

		var value = 3;
		var min = 5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be greater than min");
	}

	@Test
	public void shouldPassWhenValueOneMoreThanMin() {

		// arrange

		var value = 6;
		var min = 5;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

		// assert

		assertThat(result).isEqualTo(6);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -3;
		var min = -5;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

		// assert

		assertThat(result).isEqualTo(-3);
	}

	@Test
	public void shouldThrowWhenNegativeValueNotGreater() {

		// arrange

		var value = -5;
		var min = -3;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be greater than min");
	}

	@Test
	public void shouldPassWhenValueMaxInt() {

		// arrange

		var value = Integer.MAX_VALUE;
		var min = Integer.MAX_VALUE - 1;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

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
			ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be greater than min");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = 3;
		var min = 5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThan(value, min, () -> new IllegalStateException("Custom error"));
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

		ValidationUtils.requireGreaterThan(value, min, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleZeroBoundary() {

		// arrange

		var positiveValue = 1;
		var zero = 0;
		var negativeValue = -1;

		// act

		var result = ValidationUtils.requireGreaterThan(positiveValue, negativeValue, "Must be greater");

		// assert

		assertThat(result).isEqualTo(1);
		var thrown1 = catchThrowable(() -> {
			ValidationUtils.requireGreaterThan(zero, zero, "Must be greater");
		});
		assertThat(thrown1)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be greater");
		var thrown2 = catchThrowable(() -> {
			ValidationUtils.requireGreaterThan(negativeValue, zero, "Must be greater");
		});
		assertThat(thrown2)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be greater");
	}
}
