package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireLessThan_longTests {

	@Test
	public void shouldPassWhenValueLessThanMax() {

		// arrange

		var value = 5L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

		// assert

		assertThat(result).isEqualTo(5L);
	}

	@Test
	public void shouldThrowWhenValueEqualsMax() {

		// arrange

		var value = 10L;
		var max = 10L;

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

		var value = 15L;
		var max = 10L;

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

		var value = 9L;
		var max = 10L;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

		// assert

		assertThat(result).isEqualTo(9L);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -5L;
		var max = -3L;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

		// assert

		assertThat(result).isEqualTo(-5L);
	}

	@Test
	public void shouldThrowWhenNegativeValueNotLess() {

		// arrange

		var value = -3L;
		var max = -5L;

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
	public void shouldPassWhenValueMinLong() {

		// arrange

		var value = Long.MIN_VALUE;
		var max = Long.MIN_VALUE + 1;

		// act

		var result = ValidationUtils.requireLessThan(value, max, "Value must be less than max");

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

		var value = 15L;
		var max = 10L;

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

		var value = 5L;
		var max = 10L;
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
	public void shouldHandleTimestamps() {

		// arrange

		var past = System.currentTimeMillis() - 10000;
		var now = System.currentTimeMillis();

		// act

		var result = ValidationUtils.requireLessThan(past, now, "Timestamp must be before now");

		// assert

		assertThat(result).isEqualTo(past);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireLessThan(now, past, "Timestamp must be before now");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Timestamp must be before now");
	}
}
