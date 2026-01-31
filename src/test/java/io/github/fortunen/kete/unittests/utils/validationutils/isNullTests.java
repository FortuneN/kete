package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNullTests {

	@Test
	public void shouldReturnTrueWhenObjectNull() {

		// arrange

		Object value = null;

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenObjectNonNull() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenEmptyString() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenZero() {

		// arrange

		var value = 0;

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenFalse() {

		// arrange

		var value = false;

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenStringNull() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenIntegerNull() {

		// arrange

		Integer value = null;

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenComplexObject() {

		// arrange

		var value = new StringBuilder("test");

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenComplexObjectNull() {

		// arrange

		StringBuilder value = null;

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenEmptyArray() {

		// arrange

		var value = new String[0];

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenArrayNull() {

		// arrange

		String[] value = null;

		// act

		var result = ValidationUtils.isNull(value);

		// assert

		assertThat(result).isTrue();
	}
}
