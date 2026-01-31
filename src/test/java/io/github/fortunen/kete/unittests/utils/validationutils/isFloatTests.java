package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isFloatTests {

	@Test
	public void shouldReturnTrueForValidPositiveFloat() {

		// act

		var result = ValidationUtils.isFloat("123.456");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeFloat() {

		// act

		var result = ValidationUtils.isFloat("-789.012");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isFloat("0.0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForScientificNotation() {

		// act

		var result = ValidationUtils.isFloat("1.23E5");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForWholeNumber() {

		// act

		var result = ValidationUtils.isFloat("42");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isFloat(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isFloat("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isFloat("not-a-float");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForInfinity() {

		// act

		var result = ValidationUtils.isFloat("Infinity");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForNaN() {

		// act

		var result = ValidationUtils.isFloat("NaN");

		// assert

		assertThat(result).isTrue();
	}
}
