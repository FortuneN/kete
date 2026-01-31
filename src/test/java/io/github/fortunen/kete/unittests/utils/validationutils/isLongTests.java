package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isLongTests {

	@Test
	public void shouldReturnTrueForValidPositiveLong() {

		// act

		var result = ValidationUtils.isLong("123456789012345");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForValidNegativeLong() {

		// act

		var result = ValidationUtils.isLong("-987654321098765");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isLong("0");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMaxLong() {

		// act

		var result = ValidationUtils.isLong(String.valueOf(Long.MAX_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMinLong() {

		// act

		var result = ValidationUtils.isLong(String.valueOf(Long.MIN_VALUE));

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isLong(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isLong("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNonNumericString() {

		// act

		var result = ValidationUtils.isLong("xyz");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForDecimalNumber() {

		// act

		var result = ValidationUtils.isLong("123.45");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForOverflowValue() {

		// act

		var result = ValidationUtils.isLong("9223372036854775808");

		// assert

		assertThat(result).isFalse();
	}
}
