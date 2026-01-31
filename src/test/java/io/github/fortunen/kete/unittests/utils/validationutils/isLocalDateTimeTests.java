package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isLocalDateTimeTests {

	@Test
	public void shouldReturnTrueForValidIsoDateTime() {

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15T10:30:00");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForDateTimeWithMillis() {

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15T10:30:00.123");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForDateTimeWithNanos() {

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15T10:30:00.123456789");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isLocalDateTime(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isLocalDateTime("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForDateOnly() {

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidFormat() {

		// act

		var result = ValidationUtils.isLocalDateTime("15-01-2024 10:30:00");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidTime() {

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15T25:00:00");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForMidnight() {

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15T00:00:00");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForEndOfDay() {

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15T23:59:59");

		// assert

		assertThat(result).isTrue();
	}
}
