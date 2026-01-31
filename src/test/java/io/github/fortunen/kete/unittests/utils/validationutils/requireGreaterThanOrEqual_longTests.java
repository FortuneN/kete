package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireGreaterThanOrEqual_longTests {

	@Test
	public void shouldPassWhenValueGreaterThanMin() {

		// arrange

		var value = 10L;
		var min = 5L;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldPassWhenValueEqualsMin() {

		// arrange

		var value = 5L;
		var min = 5L;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(5L);
	}

	@Test
	public void shouldThrowWhenValueLessThanMin() {

		// arrange

		var value = 3L;
		var min = 5L;

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

		var value = 6L;
		var min = 5L;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(6L);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -3L;
		var min = -5L;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(-3L);
	}

	@Test
	public void shouldThrowWhenNegativeValueLess() {

		// arrange

		var value = -5L;
		var min = -3L;

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
	public void shouldPassWhenValueMaxLong() {

		// arrange

		var value = Long.MAX_VALUE;
		var min = Long.MAX_VALUE;

		// act

		var result = ValidationUtils.requireGreaterThanOrEqual(value, min, "Value must be >= min");

		// assert

		assertThat(result).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void shouldThrowWhenValueMinLong() {

		// arrange

		var value = Long.MIN_VALUE;
		var min = 0L;

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

		var value = 3L;
		var min = 5L;

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

		var value = 10L;
		var min = 5L;
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
	public void shouldHandleTimestamps() {

		// arrange

		var now = System.currentTimeMillis();
		var past = now - 10000;

		// act

		var result1 = ValidationUtils.requireGreaterThanOrEqual(now, now, "Timestamp must be >= threshold");
		var result2 = ValidationUtils.requireGreaterThanOrEqual(now, past, "Timestamp must be >= threshold");

		// assert

		assertThat(result1).isEqualTo(now);
		assertThat(result2).isEqualTo(now);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThanOrEqual(past, now, "Timestamp must be >= threshold");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Timestamp must be >= threshold");
	}
}
