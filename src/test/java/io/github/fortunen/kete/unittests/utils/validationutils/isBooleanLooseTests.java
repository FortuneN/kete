package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isBooleanLooseTests {

	@Test
	public void shouldReturnTrueForLowercaseTrue() {

		// act

		var result = ValidationUtils.isBooleanLoose("true");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUppercaseTrue() {

		// act

		var result = ValidationUtils.isBooleanLoose("TRUE");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMixedCaseTrue() {

		// act

		var result = ValidationUtils.isBooleanLoose("True");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForLowercaseFalse() {

		// act

		var result = ValidationUtils.isBooleanLoose("false");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUppercaseFalse() {

		// act

		var result = ValidationUtils.isBooleanLoose("FALSE");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMixedCaseFalse() {

		// act

		var result = ValidationUtils.isBooleanLoose("False");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isBooleanLoose(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isBooleanLoose("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForYes() {

		// act

		var result = ValidationUtils.isBooleanLoose("yes");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForNo() {

		// act

		var result = ValidationUtils.isBooleanLoose("no");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForOne() {

		// act

		var result = ValidationUtils.isBooleanLoose("1");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZero() {

		// act

		var result = ValidationUtils.isBooleanLoose("0");

		// assert

		assertThat(result).isTrue();
	}
}
