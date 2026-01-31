package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireInRange_intTests {

	@Test
	public void shouldPassWhenValueInRange() {

		// arrange

		var value = 5;
		var min = 1;
		var max = 10;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldPassWhenValueEqualsMin() {

		// arrange

		var value = 1;
		var min = 1;
		var max = 10;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(1);
	}

	@Test
	public void shouldPassWhenValueEqualsMax() {

		// arrange

		var value = 10;
		var min = 1;
		var max = 10;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldThrowWhenValueBelowMin() {

		// arrange

		var value = 0;
		var min = 1;
		var max = 10;

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

		var value = 11;
		var min = 1;
		var max = 10;

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

		var value = -5;
		var min = -10;
		var max = -1;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(-5);
	}

	@Test
	public void shouldPassWhenRangeSpansZero() {

		// arrange

		var value = 0;
		var min = -10;
		var max = 10;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldPassWhenRangeIsSingleValue() {

		// arrange

		var value = 5;
		var min = 5;
		var max = 5;

		// act

		var result = ValidationUtils.requireInRange(value, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = 100;
		var min = 1;
		var max = 10;

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

		var value = 5;
		var min = 1;
		var max = 10;
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
	public void shouldHandleMaxIntBoundaries() {

		// arrange

		var valueInRange = Integer.MAX_VALUE - 1;
		var valueOutOfRange = Integer.MIN_VALUE;
		var min = 0;
		var max = Integer.MAX_VALUE;

		// act

		var result = ValidationUtils.requireInRange(valueInRange, min, max, "Value must be in range");

		// assert

		assertThat(result).isEqualTo(Integer.MAX_VALUE - 1);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(valueOutOfRange, min, max, "Value must be in range");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be in range");
	}

	@Test
	public void shouldHandlePortRange() {

		// arrange

		var validPort = 8080;
		var invalidPort = 70000;
		var minPort = 1;
		var maxPort = 65535;

		// act

		var result = ValidationUtils.requireInRange(validPort, minPort, maxPort, "Port must be valid");

		// assert

		assertThat(result).isEqualTo(8080);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(invalidPort, minPort, maxPort, "Port must be valid");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Port must be valid");
	}

	@Test
	public void shouldThrowWhenBelowMinWithSupplier() {

		// arrange

		var value = 0;
		var min = 1;
		var max = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInRange(value, min, max, () -> new IllegalStateException("Below min error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Below min error");
	}
}
