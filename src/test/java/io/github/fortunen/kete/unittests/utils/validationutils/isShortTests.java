package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isShortTests {

	@Test
	public void shouldReturnTrueForValidPositiveShort() {

		// act

		var result = ValidationUtils.isShort("12345");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeShort() {

		// act

		var result = ValidationUtils.isShort("-12345");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isShort("0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMaxShort() {

		// act

		var result = ValidationUtils.isShort(String.valueOf(Short.MAX_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMinShort() {

		// act

		var result = ValidationUtils.isShort(String.valueOf(Short.MIN_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isShort(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isShort("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForOverflowValue() {

		// act

		var result = ValidationUtils.isShort("32768");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForUnderflowValue() {

		// act

		var result = ValidationUtils.isShort("-32769");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isShort("abc");

		// assert

		assertThat(result).isFalse();
	}
}
