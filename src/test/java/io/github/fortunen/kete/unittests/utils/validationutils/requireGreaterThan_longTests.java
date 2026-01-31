package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireGreaterThan_longTests {

	@Test
	public void shouldPassWhenValueGreaterThanMin() {

		// arrange

		var value = 10L;
		var min = 5L;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

		// assert

		assertThat(result).isEqualTo(10L);
	}

	@Test
	public void shouldThrowWhenValueEqualsMin() {

		// arrange

		var value = 5L;
		var min = 5L;

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

		var value = 3L;
		var min = 5L;

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

		var value = 6L;
		var min = 5L;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

		// assert

		assertThat(result).isEqualTo(6L);
	}

	@Test
	public void shouldHandleNegativeValues() {

		// arrange

		var value = -3L;
		var min = -5L;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

		// assert

		assertThat(result).isEqualTo(-3L);
	}

	@Test
	public void shouldThrowWhenNegativeValueNotGreater() {

		// arrange

		var value = -5L;
		var min = -3L;

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
	public void shouldPassWhenValueMaxLong() {

		// arrange

		var value = Long.MAX_VALUE;
		var min = Long.MAX_VALUE - 1;

		// act

		var result = ValidationUtils.requireGreaterThan(value, min, "Value must be greater than min");

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

		var value = 3L;
		var min = 5L;

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

		var value = 10L;
		var min = 5L;
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
	public void shouldHandleTimestamps() {

		// arrange

		var now = System.currentTimeMillis();
		var past = now - 10000;

		// act

		var result = ValidationUtils.requireGreaterThan(now, past, "Timestamp must be after past");

		// assert

		assertThat(result).isEqualTo(now);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireGreaterThan(past, now, "Timestamp must be after past");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Timestamp must be after past");
	}
}
