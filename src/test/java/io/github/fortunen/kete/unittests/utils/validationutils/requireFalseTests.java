package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class requireFalseTests {

	@Test
	public void shouldPassWhenConditionFalse() {

		// arrange

		var condition = false;

		// act & assert

		ValidationUtils.requireFalse(condition, "must be false");
	}

	@Test
	public void shouldThrowWhenConditionTrue() {

		// arrange

		var condition = true;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireFalse(condition, "should not be true");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("should not be true");
	}

	@Test
	public void shouldHandleNegationLogic() {

		// arrange

		var value = 100;

		// act & assert

		ValidationUtils.requireFalse(value < 0, "value cannot be negative");
	}

	@Test
	public void shouldHandleBooleanSupplierFalse() {

		// arrange

		BooleanSupplier supplier = () -> 2 + 2 == 5;

		// act & assert

		ValidationUtils.requireFalse(supplier, "math failed");
	}

	@Test
	public void shouldThrowWhenBooleanSupplierReturnsTrue() {

		// arrange

		var value = 4;
		BooleanSupplier supplier = () -> 2 + 2 == value;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireFalse(supplier, "should be false");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("should be false");
	}

	@Test
	public void shouldHandleSupplierWithSideEffects() {

		// arrange

		var counter = new int[]{0};
		BooleanSupplier supplier = () -> {
			counter[0]++;
			return false;
		};

		// act

		ValidationUtils.requireFalse(supplier, "counter failed");

		// assert

		assertThat(counter[0]).isEqualTo(1);
	}

	@Test
	public void shouldThrowCustomExceptionFromSupplier() {

		// arrange

		var condition = true;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireFalse(condition, () -> new UnsupportedOperationException("not allowed"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("not allowed");
	}

	@Test
	public void shouldNotEvaluateSupplierWhenConditionFalse() {

		// arrange

		var condition = false;
		var invoked = new boolean[]{false};

		// act

		ValidationUtils.requireFalse(condition, () -> {
			invoked[0] = true;
			return new IllegalStateException("should not be called");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleComplexBooleanExpressions() {

		// arrange

		var text = "hello";

		// act & assert

		ValidationUtils.requireFalse(text == null || text.isEmpty(), "text is empty");
	}

	@Test
	public void shouldHandleDoubleNegation() {

		// arrange

		var isValid = true;

		// act & assert

		ValidationUtils.requireFalse(!isValid, "must be invalid");
	}

	@Test
	public void shouldThrowWithEmptyMessage() {

		// arrange

		var condition = true;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireFalse(condition, "");
		});

		// assert

		assertThat(thrown)
			.hasMessage("");
	}

	// BooleanSupplier + Supplier<? extends Throwable> overload tests (@SneakyThrows)

	@Test
	public void shouldPassWhenBooleanSupplierReturnsFalseWithExceptionSupplier() {

		// arrange

		BooleanSupplier conditionSupplier = () -> false;
		Supplier<IllegalStateException> exceptionSupplier = () -> new IllegalStateException("should not throw");

		// act & assert (no exception thrown)

		ValidationUtils.requireFalse(conditionSupplier, exceptionSupplier);
	}

	@Test
	public void shouldThrowCustomExceptionWhenBooleanSupplierReturnsTrue() {

		// arrange

		BooleanSupplier conditionSupplier = () -> true;
		Supplier<UnsupportedOperationException> exceptionSupplier = () -> new UnsupportedOperationException("condition was true");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireFalse(conditionSupplier, exceptionSupplier);
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(UnsupportedOperationException.class)
			.hasMessage("condition was true");
	}

	@Test
	public void shouldNotEvaluateExceptionSupplierWhenBooleanSupplierReturnsFalse() {

		// arrange

		BooleanSupplier conditionSupplier = () -> false;
		var exceptionSupplierInvoked = new boolean[]{false};
		Supplier<IllegalStateException> exceptionSupplier = () -> {
			exceptionSupplierInvoked[0] = true;
			return new IllegalStateException("should not be created");
		};

		// act

		ValidationUtils.requireFalse(conditionSupplier, exceptionSupplier);

		// assert

		assertThat(exceptionSupplierInvoked[0]).isFalse();
	}

	@Test
	public void shouldEvaluateBooleanSupplierExactlyOnceWithExceptionSupplier() {

		// arrange

		var counter = new int[]{0};
		BooleanSupplier conditionSupplier = () -> {
			counter[0]++;
			return false;
		};
		Supplier<IllegalStateException> exceptionSupplier = () -> new IllegalStateException("check failed");

		// act

		ValidationUtils.requireFalse(conditionSupplier, exceptionSupplier);

		// assert

		assertThat(counter[0]).isEqualTo(1);
	}

	@Test
	public void shouldThrowCheckedExceptionViaSneakyThrowsWithBooleanSupplier() {

		// arrange

		BooleanSupplier conditionSupplier = () -> true;
		Supplier<IOException> exceptionSupplier = () -> new IOException("IO error");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireFalse(conditionSupplier, exceptionSupplier);
		});

		// assert - @SneakyThrows allows checked exception without declaring

		assertThat(thrown)
			.isInstanceOf(IOException.class)
			.hasMessage("IO error");
	}
}
