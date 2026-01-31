package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireInRange_longTests {

	@Test
	public void shouldPassWhenValueInRange() {

		// arrange

		var value = 5L;
		var min = 1L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(5L);
	}

	@Test
	public void shouldPassWhenValueEqualsMin() {

		// arrange

		var value = 1L;
		var min = 1L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(1L);
	}

	@Test
	public void shouldPassWhenValueEqualsMax() {

		// arrange

		var value = 10L;
		var min = 1L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldThrowWhenValueBelowMin() {

		// arrange

		var value = 0L;
		var min = 1L;
		var max = 10L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(value, min, max, "Value must be in range");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Value must be in range");
	}

	@Test
	public void shouldThrowWhenValueAboveMax() {

		// arrange

		var value = 11L;
		var min = 1L;
		var max = 10L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(value, min, max, "Value must be in range");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be in range");
	}

	@Test
	public void shouldPassWhenValueInNegativeRange() {

		// arrange

		var value = -5L;
		var min = -10L;
		var max = -1L;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(-5L);
	}

	@Test
	public void shouldPassWhenRangeSpansZero() {

		// arrange

		var value = 0L;
		var min = -10L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(0L);
	}

	@Test
	public void shouldPassWhenRangeIsSingleValue() {

		// arrange

		var value = 5L;
		var min = 5L;
		var max = 5L;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(5L);
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = 100L;
		var min = 1L;
		var max = 10L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(value, min, max, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = 5L;
		var min = 1L;
		var max = 10L;
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireInRange(value, min, max, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleMaxLongBoundaries() {

		// arrange

		var valueInRange = Long.MAX_VALUE - 1;
		var valueOutOfRange = Long.MIN_VALUE;
		var min = 0L;
		var max = Long.MAX_VALUE;

		// act

		var result = ValidationUtils.requireInRange(valueInRange, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(Long.MAX_VALUE - 1);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(valueOutOfRange, min, max, "Value must be in range");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be in range");
	}

	@Test
	public void shouldHandleTimestampRange() {

		// arrange

		var now = System.currentTimeMillis();
		var oneYearAgo = now - (365L * 24 * 60 * 60 * 1000);
		var oneYearFromNow = now + (365L * 24 * 60 * 60 * 1000);
		var veryOld = 0L;

		// act

		var result = ValidationUtils.requireInRange(now, oneYearAgo, oneYearFromNow, "Timestamp must be within one year");

		// assert

		assertThat(result).isEqualTo(now);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(veryOld, oneYearAgo, oneYearFromNow, "Timestamp must be within one year");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Timestamp must be within one year");
	}

	@Test
	public void shouldThrowWhenBelowMinWithSupplier() {

		// arrange

		var value = 0L;
		var min = 1L;
		var max = 10L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(value, min, max, () -> new IllegalStateException("Below min error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Below min error");
	}

	@Test
	public void shouldThrowWhenAboveMaxWithSupplier() {

		// arrange

		var value = 100L;
		var min = 1L;
		var max = 10L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(value, min, max, () -> new IllegalStateException("Above max error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Above max error");
	}
}
