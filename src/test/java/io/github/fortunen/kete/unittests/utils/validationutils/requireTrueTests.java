package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class requireTrueTests {

	@Test
	public void shouldPassWhenConditionTrue() {

		// arrange

		var condition = true;

		// act & assert

		ValidationUtils.requireTrue(condition, "test message");
	}

	@Test
	public void shouldThrowWhenConditionFalse() {

		// arrange

		var condition = false;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(condition, "must be true");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("must be true");
	}

	@Test
	public void shouldHandleComplexBooleanExpressions() {

		// arrange

		var value = 50;

		// act & assert

		ValidationUtils.requireTrue(value > 0 && value < 100, "value out of range");
	}

	@Test
	public void shouldThrowWithNullMessage() {

		// arrange

		var condition = false;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(condition, (String) null);
		});

		// assert

		assertThat(thrown.getMessage()).isNull();
	}

	@Test
	public void shouldHandleNestedValidations() {

		// arrange

		var outer = true;
		var inner = false;

		// act

		ValidationUtils.requireTrue(outer, "outer failed");

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(inner, "inner failed");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("inner failed");
	}

	@Test
	public void shouldThrowCustomExceptionFromSupplier() {

		// arrange

		var condition = false;
		Supplier<CustomException> exceptionSupplier = () -> new CustomException("custom error");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(condition, exceptionSupplier);
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("custom error");
	}

	@Test
	public void shouldHandleSupplierReturningRuntimeException() {

		// arrange

		var condition = false;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(condition, () -> new UnsupportedOperationException("not supported"));
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("not supported");
	}

	private static class CustomException extends Exception {
		public CustomException(String message) {
			super(message);
		}
	}
}
