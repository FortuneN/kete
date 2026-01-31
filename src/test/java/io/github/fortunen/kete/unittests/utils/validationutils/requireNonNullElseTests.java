package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonNullElseTests {

	@Test
	public void shouldReturnValueWhenNonNull() {

		// arrange

		var value = "test";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("test");
	}

	@Test
	public void shouldReturnDefaultValueWhenNull() {

		// arrange

		String value = null;
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnNullDefaultWhenBothNull() {

		// arrange

		String value = null;
		String defaultValue = null;

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isNull();
	}

	@Test
	public void shouldHandleIntegerValues() {

		// arrange

		Integer value = null;
		var defaultValue = 42;

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(42);
	}

	@Test
	public void shouldHandleEmptyString() {

		// arrange

		var value = "";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldHandleZeroValue() {

		// arrange

		var value = 0;
		var defaultValue = 42;

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldHandleFalseValue() {

		// arrange

		var value = false;
		var defaultValue = true;

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(false);
	}

	@Test
	public void shouldHandleComplexObjects() {

		// arrange

		var value = new StringBuilder("test");
		var defaultValue = new StringBuilder("default");

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result.toString()).isEqualTo("test");
	}

	@Test
	public void shouldHandleNullComplexObject() {

		// arrange

		StringBuilder value = null;
		var defaultValue = new StringBuilder("default");

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result.toString()).isEqualTo("default");
	}

	@Test
	public void shouldHandleArrays() {

		// arrange

		String[] value = null;
		var defaultValue = new String[] { "a", "b" };

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result.length).isEqualTo(2);
		assertThat(result[0]).isEqualTo("a");
	}

	@Test
	public void shouldNotInvokeDefaultForNonNullValue() {

		// arrange

		var value = "test";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonNullElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("test");
	}
}
