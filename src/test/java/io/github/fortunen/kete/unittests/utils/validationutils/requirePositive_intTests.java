package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.ValidationUtils;

public class requirePositive_intTests {

	@Test
	public void shouldReturnValueWhenPositive() {

		// arrange

		var value = 42;

		// act

		var result = ValidationUtils.requirePositive(value, "must be positive");

		// assert

		assertThat(result).isEqualTo(42);
	}

	@Test
	public void shouldThrowWhenValueIsZero() {

		// arrange

		var value = 0;

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

		var value = -1;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, "cannot be negative");
		});

		// assert

		assertThat(thrown).isNotNull();

		assertThat(thrown)
			.hasMessage("cannot be negative");
	}

	@Test
	public void shouldReturnValueWhenMaxInt() {

		// arrange

		var value = Integer.MAX_VALUE;

		// act

		var result = ValidationUtils.requirePositive(value, "must be positive");

		// assert

		assertThat(result).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void shouldThrowWhenValueIsMinInt() {

		// arrange

		var value = Integer.MIN_VALUE;

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

		var value = 1;

		// act

		var result = ValidationUtils.requirePositive(value, "must be positive");

		// assert

		assertThat(result).isEqualTo(1);
	}

	@Test
	public void shouldThrowWhenValueIsNegativeOne() {

		// arrange

		var value = -1;

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
	public void shouldThrowCustomExceptionFromSupplier() {

		// arrange

		var value = 0;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, () -> new IllegalStateException("custom"));
		});

		// assert

		assertThat(thrown).isNotNull();

		assertThat(thrown)
			.hasMessage("custom");
	}

	@Test
	public void shouldNotInvokeSupplierWhenValuePositive() {

		// arrange

		var value = 100;
		var invoked = new boolean[]{false};

		// act

		var result = ValidationUtils.requirePositive(value, () -> {
			invoked[0] = true;
			return new IllegalStateException("should not be called");
		});

		// assert

		assertThat(result).isEqualTo(100);
		assertThat(invoked[0]).isEqualTo(false);
	}

	// @SneakyThrows exception path tests

	@Test
	public void shouldThrowCheckedExceptionViaSneakyThrows() {

		// arrange

		var value = 0;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, () -> new IOException("IO error"));
		});

		// assert - @SneakyThrows allows checked exception without declaring

		assertThat(thrown)
			.isInstanceOf(IOException.class)
			.hasMessage("IO error");
	}

	@Test
	public void shouldThrowCheckedExceptionForNegativeValue() {

		// arrange

		var value = -5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requirePositive(value, () -> new java.sql.SQLException("negative value error"));
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(java.sql.SQLException.class)
			.hasMessage("negative value error");
	}
}
