package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class tryParseInstantTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("   ")).isEmpty();
	}

	@Test
	public void shouldParseIsoInstant() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("2024-03-15T10:30:00Z"))
			.hasValue(Instant.parse("2024-03-15T10:30:00Z"));
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("  2024-03-15T10:30:00Z  "))
			.hasValue(Instant.parse("2024-03-15T10:30:00Z"));
	}

	@Test
	public void shouldParseInstantWithOffset() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("2024-03-15T10:30:00+02:00"))
			.hasValue(Instant.parse("2024-03-15T08:30:00Z"));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormat() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("2024-03-15 10:30:00")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForDateOnly() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("2024-03-15")).isEmpty();
	}

	@Test
	public void shouldParseEpoch() {

		// act & assert

		assertThat(ValidationUtils.tryParseInstant("1970-01-01T00:00:00Z")).hasValue(Instant.EPOCH);
	}
}
