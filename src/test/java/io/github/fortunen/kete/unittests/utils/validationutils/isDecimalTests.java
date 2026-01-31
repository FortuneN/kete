package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isDecimalTests {

	@Test
	public void shouldReturnTrueForValidPositiveDecimal() {

		// act

		var result = ValidationUtils.isDecimal("123.456789012345678901234567890");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeDecimal() {

		// act

		var result = ValidationUtils.isDecimal("-987.654321098765432109876543210");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isDecimal("0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForLargeNumber() {

		// act

		var result = ValidationUtils.isDecimal("12345678901234567890123456789012345678901234567890");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForWholeNumber() {

		// act

		var result = ValidationUtils.isDecimal("42");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isDecimal(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isDecimal("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isDecimal("not-a-decimal");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInfinity() {

		// act

		var result = ValidationUtils.isDecimal("Infinity");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForScientificNotation() {

		// act

		var result = ValidationUtils.isDecimal("1.23E+10");

		// assert

		assertThat(result).isTrue();
	}
}
