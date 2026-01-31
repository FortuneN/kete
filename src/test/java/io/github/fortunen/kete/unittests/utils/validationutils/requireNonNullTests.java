package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class requireNonNullTests {

	@Test
	public void shouldReturnValueWhenNotNull() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.requireNonNull(value, "cannot be null");

		// assert

		assertThat(result).isEqualTo("test");
		assertThat(result).isSameAs(value);
	}

	@Test
	public void shouldThrowWhenValueIsNull() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNull(value, "value is required");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("value is required");
	}

	@Test
	public void shouldReturnComplexObjectWhenNotNull() {

		// arrange

		var obj = new ComplexObject("data", 42);

		// act

		var result = ValidationUtils.requireNonNull(obj, "object required");

		// assert

		assertThat(result).isSameAs(obj);
		assertThat(result.data).isEqualTo("data");
		assertThat(result.number).isEqualTo(42);
	}

	@Test
	public void shouldThrowCustomExceptionFromSupplier() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNull(value, () -> new IllegalStateException("custom null"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("custom null");
	}

	@Test
	public void shouldNotInvokeSupplierWhenValueNotNull() {

		// arrange

		var value = "present";
		var invoked = new boolean[]{false};

		// act

		var result = ValidationUtils.requireNonNull(value, () -> {
			invoked[0] = true;
			return new IllegalStateException("should not be called");
		});

		// assert

		assertThat(result).isEqualTo("present");
		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleEmptyString() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.requireNonNull(value, "cannot be null");

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldHandleZeroInteger() {

		// arrange

		var value = Integer.valueOf(0);

		// act

		var result = ValidationUtils.requireNonNull(value, "cannot be null");

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldHandleFalseBoolean() {

		// arrange

		var value = Boolean.FALSE;

		// act

		var result = ValidationUtils.requireNonNull(value, "cannot be null");

		// assert

		assertThat(result).isEqualTo(false);
	}

	@Test
	public void shouldPreserveNullMessage() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNull(value, (String) null);
		});

		// assert

		assertThat(thrown.getMessage()).isNull();
	}

	@Test
	public void shouldHandleArrays() {

		// arrange

		var array = new String[]{"a", "b", "c"};

		// act

		var result = ValidationUtils.requireNonNull(array, "array required");

		// assert

		assertThat(result).isSameAs(array);
		assertThat(result.length).isEqualTo(3);
	}

	@Test
	public void shouldHandleNestedNullChecks() {

		// arrange

		var outer = new ComplexObject("outer", 1);
		String inner = null;

		// act

		var outerResult = ValidationUtils.requireNonNull(outer, "outer required");

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNull(inner, "inner required");
		});

		// assert

		assertThat(outerResult).isSameAs(outer);
		assertThat(thrown.getMessage()).isEqualTo("inner required");
	}

	private static class ComplexObject {
		String data;
		int number;

		ComplexObject(String data, int number) {
			this.data = data;
			this.number = number;
		}
	}

	// @SneakyThrows exception path tests

	@Test
	public void shouldThrowCheckedExceptionViaSneakyThrows() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNull(value, () -> new IOException("IO error"));
		});

		// assert - @SneakyThrows allows checked exception without declaring

		assertThat(thrown)
			.isInstanceOf(IOException.class)
			.hasMessage("IO error");
	}

	@Test
	public void shouldThrowCustomCheckedExceptionFromSupplier() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonNull(value, () -> new CustomCheckedException("custom checked error"));
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(CustomCheckedException.class)
			.hasMessage("custom checked error");
	}

	private static class CustomCheckedException extends Exception {
		public CustomCheckedException(String message) {
			super(message);
		}
	}
}
