package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isOffsetDateTimeTests {

	@Test
	public void shouldReturnTrueForValidOffsetDateTime() {

		// act

		var result = ValidationUtils.isOffsetDateTime("2024-01-15T10:30:00+05:30");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUtcOffset() {

		// act

		var result = ValidationUtils.isOffsetDateTime("2024-01-15T10:30:00+00:00");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForZuluTime() {

		// act

		var result = ValidationUtils.isOffsetDateTime("2024-01-15T10:30:00Z");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForNegativeOffset() {

		// act

		var result = ValidationUtils.isOffsetDateTime("2024-01-15T10:30:00-08:00");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isOffsetDateTime(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isOffsetDateTime("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForLocalDateTime() {

		// act

		var result = ValidationUtils.isOffsetDateTime("2024-01-15T10:30:00");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidFormat() {

		// act

		var result = ValidationUtils.isOffsetDateTime("not-a-datetime");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForWithMillis() {

		// act

		var result = ValidationUtils.isOffsetDateTime("2024-01-15T10:30:00.123+05:30");

		// assert

		assertThat(result).isTrue();
	}
}
