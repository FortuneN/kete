package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNotNullTests {

	@Test
	public void shouldReturnFalseWhenObjectNull() {

		// arrange

		Object value = null;

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenObjectNonNull() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenEmptyString() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenZero() {

		// arrange

		var value = 0;

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenFalse() {

		// arrange

		var value = false;

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenStringNull() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenIntegerNull() {

		// arrange

		Integer value = null;

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenComplexObject() {

		// arrange

		var value = new StringBuilder("test");

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenComplexObjectNull() {

		// arrange

		StringBuilder value = null;

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenEmptyArray() {

		// arrange

		var value = new String[0];

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenArrayNull() {

		// arrange

		String[] value = null;

		// act

		var result = ValidationUtils.isNotNull(value);

		// assert

		assertThat(result).isFalse();
	}
}
