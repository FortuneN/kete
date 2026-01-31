package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isDoubleTests {

	@Test
	public void shouldReturnTrueForValidPositiveDouble() {

		// act

		var result = ValidationUtils.isDouble("123.456");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeDouble() {

		// act

		var result = ValidationUtils.isDouble("-789.012");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isDouble("0.0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForScientificNotation() {

		// act

		var result = ValidationUtils.isDouble("1.23E10");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForWholeNumber() {

		// act

		var result = ValidationUtils.isDouble("42");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isDouble(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isDouble("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isDouble("not-a-number");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForInfinity() {

		// act

		var result = ValidationUtils.isDouble("Infinity");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForNegativeInfinity() {

		// act

		var result = ValidationUtils.isDouble("-Infinity");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForNaN() {

		// act

		var result = ValidationUtils.isDouble("NaN");

		// assert

		assertThat(result).isTrue();
	}
}
