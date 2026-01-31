package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonEmpty_ArrayTests {

	@Test
	public void shouldPassWhenArrayHasElements() {

		// arrange

		var array = new String[] { "a", "b", "c" };

		// act

		var result = ValidationUtils.requireNonEmpty(array, "Array must not be empty");

		// assert

		assertThat(result.length).isEqualTo(3);
	}

	@Test
	public void shouldThrowWhenArrayNull() {

		// arrange

		String[] array = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(array, "Array must not be empty");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Array must not be empty");
	}

	@Test
	public void shouldThrowWhenArrayEmpty() {

		// arrange

		var array = new String[0];

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(array, "Array must not be empty");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Array must not be empty");
	}

	@Test
	public void shouldPassWhenArrayHasSingleElement() {

		// arrange

		var array = new String[] { "single" };

		// act

		var result = ValidationUtils.requireNonEmpty(array, "Array must not be empty");

		// assert

		assertThat(result.length).isEqualTo(1);
	}

	@Test
	public void shouldPassWhenArrayHasNullElement() {

		// arrange

		var array = new String[] { null };

		// act

		var result = ValidationUtils.requireNonEmpty(array, "Array must not be empty");

		// assert

		assertThat(result.length).isEqualTo(1);
		assertThat(result[0]).isNull();
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var array = new String[0];

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(array, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var array = new String[] { "test" };
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireNonEmpty(array, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldReturnSameArrayInstance() {

		// arrange

		var array = new String[] { "test" };

		// act

		var result = ValidationUtils.requireNonEmpty(array, "Array must not be empty");

		// assert

		assertThat(result == array).isTrue();
	}

	@Test
	public void shouldHandlePrimitiveWrapperArray() {

		// arrange

		var intArray = new Integer[] { 1, 2, 3 };

		// act

		var result = ValidationUtils.requireNonEmpty(intArray, "Array must not be empty");

		// assert

		assertThat(result.length).isEqualTo(3);
		assertThat(result[0]).isEqualTo(1);
	}

	@Test
	public void shouldHandleObjectArray() {

		// arrange

		var objArray = new Object[] { "string", 42, "true", null };

		// act

		var result = ValidationUtils.requireNonEmpty(objArray, "Array must not be empty");

		// assert

		assertThat(result.length).isEqualTo(4);
		assertThat(result[0]).isEqualTo("string");
		assertThat(result[1]).isEqualTo(42);
	}

	@Test
	public void shouldThrowWhenNullWithSupplier() {

		// arrange

		String[] array = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(array, () -> new IllegalStateException("Custom null error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom null error");
	}
}
