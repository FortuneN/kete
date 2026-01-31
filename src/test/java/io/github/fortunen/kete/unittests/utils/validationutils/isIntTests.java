package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isIntTests {

	@Test
	public void shouldReturnTrueForValidPositiveInteger() {

		// act

		var result = ValidationUtils.isInt("123");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeInteger() {

		// act

		var result = ValidationUtils.isInt("-456");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isInt("0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMaxInt() {

		// act

		var result = ValidationUtils.isInt(String.valueOf(Integer.MAX_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMinInt() {

		// act

		var result = ValidationUtils.isInt(String.valueOf(Integer.MIN_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isInt(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isInt("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isInt("abc");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForDecimalNumber() {

		// act

		var result = ValidationUtils.isInt("123.45");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForOverflowValue() {

		// act

		var result = ValidationUtils.isInt("2147483648");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForUnderflowValue() {

		// act

		var result = ValidationUtils.isInt("-2147483649");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForWhitespaceOnly() {

		// act

		var result = ValidationUtils.isInt("   ");

		// assert

		assertThat(result).isFalse();
	}
}
