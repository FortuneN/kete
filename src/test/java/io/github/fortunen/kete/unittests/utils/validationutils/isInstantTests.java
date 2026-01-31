package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isInstantTests {

	@Test
	public void shouldReturnTrueForValidIsoInstant() {

		// act

		var result = ValidationUtils.isInstant("2024-01-15T10:30:00Z");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForInstantWithMillis() {

		// act

		var result = ValidationUtils.isInstant("2024-01-15T10:30:00.123Z");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForInstantWithNanos() {

		// act

		var result = ValidationUtils.isInstant("2024-01-15T10:30:00.123456789Z");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isInstant(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isInstant("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForLocalDateTime() {

		// act

		var result = ValidationUtils.isInstant("2024-01-15T10:30:00");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForDateOnly() {

		// act

		var result = ValidationUtils.isInstant("2024-01-15");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidFormat() {

		// act

		var result = ValidationUtils.isInstant("not-an-instant");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForEpoch() {

		// act

		var result = ValidationUtils.isInstant("1970-01-01T00:00:00Z");

		// assert

		assertThat(result).isTrue();
	}
}
