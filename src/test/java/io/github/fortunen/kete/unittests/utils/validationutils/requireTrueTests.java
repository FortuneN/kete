package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.function.BooleanSupplier;
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
	public void shouldHandleBooleanSupplier() {

		// arrange

		var value = 4;
		BooleanSupplier supplier = () -> 2 + 2 == value;

		// act & assert

		ValidationUtils.requireTrue(supplier, "math failed");
	}

	@Test
	public void shouldThrowWhenBooleanSupplierReturnsFalse() {

		// arrange

		BooleanSupplier supplier = () -> 2 + 2 == 5;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(supplier, "math check failed");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("math check failed");
	}

	@Test
	public void shouldHandleBooleanSupplierWithSideEffects() {

		// arrange

		var counter = new int[]{0};
		BooleanSupplier supplier = () -> {
			counter[0]++;
			return counter[0] > 0;
		};

		// act

		ValidationUtils.requireTrue(supplier, "counter failed");

		// assert

		assertThat(counter[0]).isEqualTo(1);
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

	@Test
	public void shouldNotEvaluateSupplierWhenConditionTrue() {

		// arrange

		var condition = true;
		var invoked = new boolean[]{false};
		Supplier<IllegalStateException> exceptionSupplier = () -> {
			invoked[0] = true;
			return new IllegalStateException("should not be called");
		};

		// act

		ValidationUtils.requireTrue(condition, exceptionSupplier);

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	// BooleanSupplier + Supplier<? extends Throwable> overload tests

	@Test
	public void shouldPassWhenBooleanSupplierReturnsTrueWithExceptionSupplier() {

		// arrange

		BooleanSupplier conditionSupplier = () -> true;
		Supplier<CustomException> exceptionSupplier = () -> new CustomException("should not throw");

		// act & assert (no exception thrown)

		ValidationUtils.requireTrue(conditionSupplier, exceptionSupplier);
	}

	@Test
	public void shouldThrowCustomExceptionWhenBooleanSupplierReturnsFalse() {

		// arrange

		BooleanSupplier conditionSupplier = () -> false;
		Supplier<CustomException> exceptionSupplier = () -> new CustomException("supplier condition failed");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(conditionSupplier, exceptionSupplier);
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(CustomException.class)
			.hasMessage("supplier condition failed");
	}

	@Test
	public void shouldNotEvaluateExceptionSupplierWhenBooleanSupplierReturnsTrue() {

		// arrange

		BooleanSupplier conditionSupplier = () -> true;
		var exceptionSupplierInvoked = new boolean[]{false};
		Supplier<IllegalStateException> exceptionSupplier = () -> {
			exceptionSupplierInvoked[0] = true;
			return new IllegalStateException("should not be created");
		};

		// act

		ValidationUtils.requireTrue(conditionSupplier, exceptionSupplier);

		// assert

		assertThat(exceptionSupplierInvoked[0]).isFalse();
	}

	@Test
	public void shouldEvaluateBooleanSupplierWithSideEffectsAndExceptionSupplier() {

		// arrange

		var counter = new int[]{0};
		BooleanSupplier conditionSupplier = () -> {
			counter[0]++;
			return counter[0] > 0;
		};
		Supplier<IllegalStateException> exceptionSupplier = () -> new IllegalStateException("counter check failed");

		// act

		ValidationUtils.requireTrue(conditionSupplier, exceptionSupplier);

		// assert

		assertThat(counter[0]).isEqualTo(1);
	}

	@Test
	public void shouldThrowRuntimeExceptionFromSupplierWhenBooleanSupplierReturnsFalse() {

		// arrange

		BooleanSupplier conditionSupplier = () -> false;
		Supplier<UnsupportedOperationException> exceptionSupplier = () -> new UnsupportedOperationException("operation not supported");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireTrue(conditionSupplier, exceptionSupplier);
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(UnsupportedOperationException.class)
			.hasMessage("operation not supported");
	}

	@Test
	public void shouldSupportComplexBooleanSupplierExpressionWithExceptionSupplier() {

		// arrange

		var value = 50;
		BooleanSupplier conditionSupplier = () -> value > 0 && value < 100;
		Supplier<IllegalArgumentException> exceptionSupplier = () -> new IllegalArgumentException("value out of range");

		// act & assert (no exception thrown)

		ValidationUtils.requireTrue(conditionSupplier, exceptionSupplier);
	}

	private static class CustomException extends Exception {
		public CustomException(String message) {
			super(message);
		}
	}
}
