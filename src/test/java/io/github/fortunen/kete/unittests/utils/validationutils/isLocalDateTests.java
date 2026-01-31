package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isLocalDateTests {

	@Test
	public void shouldReturnTrueForValidIsoDate() {

		// act

		var result = ValidationUtils.isLocalDate("2024-01-15");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForLeapYearDate() {

		// act

		var result = ValidationUtils.isLocalDate("2024-02-29");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidLeapYearDate() {

		// act

		var result = ValidationUtils.isLocalDate("2023-02-29");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isLocalDate(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isLocalDate("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidFormat() {

		// act

		var result = ValidationUtils.isLocalDate("15-01-2024");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidMonth() {

		// act

		var result = ValidationUtils.isLocalDate("2024-13-01");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidDay() {

		// act

		var result = ValidationUtils.isLocalDate("2024-01-32");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForDateTimeString() {

		// act

		var result = ValidationUtils.isLocalDate("2024-01-15T10:30:00");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForMinDate() {

		// act

		var result = ValidationUtils.isLocalDate("0001-01-01");

		// assert

		assertThat(result).isTrue();
	}
}
