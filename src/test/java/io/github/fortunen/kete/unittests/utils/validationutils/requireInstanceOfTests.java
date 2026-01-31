package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

public class requireInstanceOfTests {

	@Test
	public void shouldPassWhenValueIsInstanceOfClass() {

		// arrange

		Object value = "test string";
		var clazz = String.class;

		// act

		var result = ValidationUtils.requireInstanceOf(value, clazz, "Value must be String");

		// assert

		assertThat(result).isEqualTo("test string");
	}

	@Test
	public void shouldThrowWhenValueNull() {

		// arrange

		Object value = null;
		var clazz = String.class;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInstanceOf(value, clazz, "Value must be String");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Value must be String");
	}

	@Test
	public void shouldThrowWhenValueNotInstanceOfClass() {

		// arrange

		Object value = 42;
		var clazz = String.class;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInstanceOf(value, clazz, "Value must be String");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Value must be String");
	}

	@Test
	public void shouldPassWithSubclass() {

		// arrange

		Object value = new StringBuilder("test");
		var clazz = CharSequence.class;

		// act

		var result = ValidationUtils.requireInstanceOf(value, clazz, "Value must be CharSequence");

		// assert

		assertThat(result instanceof CharSequence).isTrue();
		assertThat(result.toString()).isEqualTo("test");
	}

	@Test
	public void shouldPassWithInterface() {

		// arrange

		Object value = new ArrayList<>();
		var clazz = List.class;

		// act

		var result = ValidationUtils.requireInstanceOf(value, clazz, "Value must be List");

		// assert

		assertThat(result instanceof List).isTrue();
	}

	@Test
	public void shouldPassWhenValueSameClass() {

		// arrange

		Object value = Integer.valueOf(42);
		var clazz = Integer.class;

		// act

		var result = ValidationUtils.requireInstanceOf(value, clazz, "Value must be Integer");

		// assert

		assertThat(result).isEqualTo(42);
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		Object value = "wrong type";
		var clazz = Integer.class;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInstanceOf(value, clazz, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		Object value = "test";
		var clazz = String.class;
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireInstanceOf(value, clazz, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldCastCorrectly() {

		// arrange

		Object value = "test";
		var clazz = String.class;

		// act

		var result = ValidationUtils.requireInstanceOf(value, clazz, "Value must be String");
		var uppercase = result.toUpperCase();

		// assert

		assertThat(uppercase).isEqualTo("TEST");
	}

	@Test
	public void shouldHandlePrimitiveWrappers() {

		// arrange

		Object intValue = 42;
		Object longValue = 42L;
		Object doubleValue = 42.0;

		// act

		var intResult = ValidationUtils.requireInstanceOf(intValue, Integer.class, "Must be Integer");
		var longResult = ValidationUtils.requireInstanceOf(longValue, Long.class, "Must be Long");
		var doubleResult = ValidationUtils.requireInstanceOf(doubleValue, Double.class, "Must be Double");

		// assert

		assertThat(intResult).isEqualTo(42);
		assertThat(longResult).isEqualTo(42L);
		assertThat(doubleResult).isEqualTo(42.0);
	}

	@Test
	public void shouldHandleInheritanceChain() {

		// arrange

		Object value = new ArrayList<>();

		// act

		var listResult = ValidationUtils.requireInstanceOf(value, List.class, "Must be List");
		var collectionResult = ValidationUtils.requireInstanceOf(value, Collection.class, "Must be Collection");

		// assert

		assertThat(listResult instanceof List).isTrue();
		assertThat(collectionResult instanceof Collection).isTrue();
		assertThat(listResult == collectionResult).isTrue();
	}

	// @SneakyThrows exception path tests

	@Test
	public void shouldThrowCheckedExceptionViaSneakyThrows() {

		// arrange

		Object value = "wrong type";
		var clazz = Integer.class;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInstanceOf(value, clazz, () -> new IOException("IO error"));
		});

		// assert - @SneakyThrows allows checked exception without declaring

		assertThat(thrown)
			.isInstanceOf(IOException.class)
			.hasMessage("IO error");
	}

	@Test
	public void shouldThrowCheckedExceptionForNullValue() {

		// arrange

		Object value = null;
		var clazz = String.class;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireInstanceOf(value, clazz, () -> new SQLException("null value error"));
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(SQLException.class)
			.hasMessage("null value error");
	}
}
