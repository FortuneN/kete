package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isDurationTests {

	@Test
	public void shouldReturnTrueForHours() {

		// act

		var result = ValidationUtils.isDuration("PT2H");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMinutes() {

		// act

		var result = ValidationUtils.isDuration("PT30M");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForSeconds() {

		// act

		var result = ValidationUtils.isDuration("PT45S");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForCombined() {

		// act

		var result = ValidationUtils.isDuration("PT2H30M45S");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForDays() {

		// act

		var result = ValidationUtils.isDuration("P1D");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForDaysAndTime() {

		// act

		var result = ValidationUtils.isDuration("P1DT2H30M");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isDuration(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isDuration("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidFormat() {

		// act

		var result = ValidationUtils.isDuration("2 hours");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForPlainNumberAsSeconds() {

		// act

		var result = ValidationUtils.isDuration("3600");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNegativeDuration() {

		// act

		var result = ValidationUtils.isDuration("-PT2H");

		// assert

		assertThat(result).isFalse();
	}
}
