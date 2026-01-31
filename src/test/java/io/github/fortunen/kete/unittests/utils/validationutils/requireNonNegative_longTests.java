package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonNegative_longTests {

	@Test
	public void shouldPassWhenValueZero() {

		// arrange

		var value = 0L;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(0L);
	}

	@Test
	public void shouldPassWhenValuePositive() {

		// arrange

		var value = 42L;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(42L);
	}

	@Test
	public void shouldThrowWhenValueNegative() {

		// arrange

		var value = -1L;

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

		var value = Long.MIN_VALUE;

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

		var value = Long.MAX_VALUE;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void shouldPassWhenValueOne() {

		// arrange

		var value = 1L;

		// act

		var result = ValidationUtils.requireNonNegative(value, "Value must be non-negative");

		// assert

		assertThat(result).isEqualTo(1L);
	}

	@Test
	public void shouldThrowWhenValueNegativeOne() {

		// arrange

		var value = -1L;

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

		var value = -100L;

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

		var value = 5L;
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
	public void shouldHandleTimestampValues() {

		// arrange

		var timestamp = System.currentTimeMillis();
		var negativeTimestamp = -timestamp;

		// act

		var result = ValidationUtils.requireNonNegative(timestamp, "Timestamp must be non-negative");

		// assert

		assertThat(result).isEqualTo(timestamp);

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNegative(negativeTimestamp, "Timestamp must be non-negative");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Timestamp must be non-negative");
	}
}
