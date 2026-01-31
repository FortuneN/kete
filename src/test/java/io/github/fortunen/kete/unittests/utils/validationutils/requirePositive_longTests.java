package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requirePositive_longTests {

	@Test
	public void shouldReturnValueWhenPositive() {

		// arrange

		var value = 42L;

		// act

		var result = ValidationUtils.requirePositive(value, "must be positive");

		// assert

		assertThat(result).isEqualTo(42L);
	}

	@Test
	public void shouldThrowWhenValueIsZero() {

		// arrange

		var value = 0L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, "must be positive");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("must be positive");
	}

	@Test
	public void shouldThrowWhenValueIsNegative() {

		// arrange

		var value = -1L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, "cannot be negative");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("cannot be negative");
	}

	@Test
	public void shouldReturnValueWhenMaxLong() {

		// arrange

		var value = Long.MAX_VALUE;

		// act

		var result = ValidationUtils.requirePositive(value, "must be positive");

		// assert

		assertThat(result).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void shouldThrowWhenValueIsMinLong() {

		// arrange

		var value = Long.MIN_VALUE;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, "must be positive");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("must be positive");
	}

	@Test
	public void shouldReturnValueWhenOne() {

		// arrange

		var value = 1L;

		// act

		var result = ValidationUtils.requirePositive(value, "must be positive");

		// assert

		assertThat(result).isEqualTo(1L);
	}

	@Test
	public void shouldThrowCustomExceptionFromSupplier() {

		// arrange

		var value = 0L;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, () -> new IllegalStateException("custom"));
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("custom");
	}

	@Test
	public void shouldNotInvokeSupplierWhenValuePositive() {

		// arrange

		var value = 100L;
		var invoked = new boolean[]{false};

		// act

		var result = ValidationUtils.requirePositive(value, () -> {
			invoked[0] = true;
			return new IllegalStateException("should not be called");
		});

		// assert

		assertThat(result).isEqualTo(100L);
		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleTimestampValues() {

		// arrange

		var value = System.currentTimeMillis();

		// act

		var result = ValidationUtils.requirePositive(value, "timestamp must be positive");

		// assert

		assertThat(result).isEqualTo(value);
	}
}
