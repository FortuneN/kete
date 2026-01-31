package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isByteTests {

	@Test
	public void shouldReturnTrueForValidPositiveByte() {

		// act

		var result = ValidationUtils.isByte("100");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeByte() {

		// act

		var result = ValidationUtils.isByte("-100");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isByte("0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMaxByte() {

		// act

		var result = ValidationUtils.isByte(String.valueOf(Byte.MAX_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMinByte() {

		// act

		var result = ValidationUtils.isByte(String.valueOf(Byte.MIN_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isByte(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isByte("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForOverflowValue() {

		// act

		var result = ValidationUtils.isByte("128");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForUnderflowValue() {

		// act

		var result = ValidationUtils.isByte("-129");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isByte("xyz");

		// assert

		assertThat(result).isFalse();
	}
}
