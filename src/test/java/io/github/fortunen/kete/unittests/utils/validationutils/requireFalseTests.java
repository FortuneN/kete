package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
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
}
