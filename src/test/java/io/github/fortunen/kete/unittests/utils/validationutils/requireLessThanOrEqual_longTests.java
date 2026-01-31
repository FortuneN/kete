package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireLessThanOrEqual_longTests {

	@Test
	public void shouldPassWhenValueLessThanMax() {

		// arrange

		var value = 5L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(5L);
	}

	@Test
	public void shouldPassWhenValueEqualsMax() {

		// arrange

		var value = 10L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldThrowWhenValueGreaterThanMax() {

		// arrange

		var value = 15L;
		var max = 10L;

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

		var value = 9L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(9L);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -5L;
		var max = -3L;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(-5L);
	}

	@Test
	public void shouldThrowWhenNegativeValueGreater() {

		// arrange

		var value = -3L;
		var max = -5L;

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
	public void shouldPassWhenValueMinLong() {

		// arrange

		var value = Long.MIN_VALUE;
		var max = Long.MIN_VALUE;

		// act

		var result = ValidationUtils.requireLessThanOrEqual(value, max, "Value must be <= max");

		// assert

		assertThat(result).isEqualTo(Long.MIN_VALUE);
	}

	@Test
	public void shouldThrowWhenValueMaxLong() {

		// arrange

		var value = Long.MAX_VALUE;
		var max = 0L;

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

		var value = 15L;
		var max = 10L;

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

		var value = 5L;
		var max = 10L;
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
	public void shouldHandleTimestamps() {

		// arrange

		var now = System.currentTimeMillis();
		var future = now + 10000;

		// act

		var result1 = ValidationUtils.requireLessThanOrEqual(now, now, "Timestamp must be <= threshold");
		var result2 = ValidationUtils.requireLessThanOrEqual(now, future, "Timestamp must be <= threshold");

		// assert

		assertThat(result1).isEqualTo(now);
		assertThat(result2).isEqualTo(now);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThanOrEqual(future, now, "Timestamp must be <= threshold");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Timestamp must be <= threshold");
	}
}
