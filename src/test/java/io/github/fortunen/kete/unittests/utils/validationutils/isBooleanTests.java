package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isBooleanTests {

	@Test
	public void shouldReturnTrueForLowercaseTrue() {

		// act

		var result = ValidationUtils.isBoolean("true");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForLowercaseFalse() {

		// act

		var result = ValidationUtils.isBoolean("false");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUppercaseTrue() {

		// act

		var result = ValidationUtils.isBoolean("TRUE");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUppercaseFalse() {

		// act

		var result = ValidationUtils.isBoolean("FALSE");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isBoolean(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isBoolean("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForYes() {

		// act

		var result = ValidationUtils.isBoolean("yes");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNo() {

		// act

		var result = ValidationUtils.isBoolean("no");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForOne() {

		// act

		var result = ValidationUtils.isBoolean("1");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForZero() {

		// act

		var result = ValidationUtils.isBoolean("0");

		// assert

		assertThat(result).isFalse();
	}
}
