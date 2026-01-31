package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isBigIntegerTests {

	@Test
	public void shouldReturnTrueForValidPositiveBigInteger() {

		// act

		var result = ValidationUtils.isBigInteger("12345678901234567890123456789012345678901234567890");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeBigInteger() {

		// act

		var result = ValidationUtils.isBigInteger("-98765432109876543210987654321098765432109876543210");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isBigInteger("0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForSmallInteger() {

		// act

		var result = ValidationUtils.isBigInteger("42");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isBigInteger(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isBigInteger("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isBigInteger("abc");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForDecimalNumber() {

		// act

		var result = ValidationUtils.isBigInteger("123.456");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForScientificNotation() {

		// act

		var result = ValidationUtils.isBigInteger("1E10");

		// assert

		assertThat(result).isFalse();
	}
}
