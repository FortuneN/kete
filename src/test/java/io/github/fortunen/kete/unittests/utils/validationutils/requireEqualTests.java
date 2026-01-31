package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireEqualTests {

	@Test
	public void shouldPassWhenValuesEqual() {

		// arrange

		var value = "test";
		var expected = "test";

		// act

		var result = ValidationUtils.requireEqual(value, expected, "Values must be equal");

		// assert

		assertThat(result).isEqualTo("test");
	}

	@Test
	public void shouldThrowWhenValuesDifferent() {

		// arrange

		var value = "test";
		var expected = "different";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, expected, "Values must be equal");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Values must be equal");
	}

	@Test
	public void shouldPassWhenBothNull() {

		// arrange

		String value = null;
		String expected = null;

		// act

		var result = ValidationUtils.requireEqual(value, expected, "Values must be equal");

		// assert

		assertThat(result).isEqualTo(null);
	}

	@Test
	public void shouldThrowWhenValueNullExpectedNotNull() {

		// arrange

		String value = null;
		var expected = "test";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, expected, "Values must be equal");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Values must be equal");
	}

	@Test
	public void shouldThrowWhenValueNotNullExpectedNull() {

		// arrange

		var value = "test";
		String expected = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, expected, "Values must be equal");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Values must be equal");
	}

	@Test
	public void shouldPassWhenIntegersEqual() {

		// arrange

		var value = 42;
		var expected = 42;

		// act

		var result = ValidationUtils.requireEqual(value, expected, "Integers must be equal");

		// assert

		assertThat(result).isEqualTo(42);
	}

	@Test
	public void shouldThrowWhenIntegersDifferent() {

		// arrange

		var value = 42;
		var expected = 43;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, expected, "Integers must be equal");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Integers must be equal");
	}

	@Test
	public void shouldPassWhenObjectsEqualByEquals() {

		// arrange

		var value = new StringBuilder("test");
		var expected = new StringBuilder("test");

		// act

		var result = ValidationUtils.requireEqual(value.toString(), expected.toString(), "Objects must be equal");

		// assert

		assertThat(result).isEqualTo("test");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = "test";
		var expected = "different";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, expected, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = "test";
		var expected = "test";
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireEqual(value, expected, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleEmptyStrings() {

		// arrange

		var value = "";
		var expected = "";

		// act

		var result = ValidationUtils.requireEqual(value, expected, "Empty strings must be equal");

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldHandleObjectReferences() {

		// arrange

		var value = new Object();
		var sameReference = value;
		var differentObject = new Object();

		// act

		var result = ValidationUtils.requireEqual(value, sameReference, "Objects must be equal");

		// assert

		assertThat(result).isEqualTo(value);
		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, differentObject, "Objects must be equal");
		});
		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Objects must be equal");
	}

	@Test
	public void shouldPassWhenBothNullWithSupplier() {

		// arrange

		String value = null;
		String expected = null;

		// act

		var result = ValidationUtils.requireEqual(value, expected, () -> new IllegalStateException("Should not be thrown"));

		// assert

		assertThat(result).isNull();
	}

	@Test
	public void shouldThrowWhenValueNullExpectedNotNullWithSupplier() {

		// arrange

		String value = null;
		var expected = "test";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, expected, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldThrowWhenValueNotNullExpectedNullWithSupplier() {

		// arrange

		var value = "test";
		String expected = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireEqual(value, expected, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}
}
