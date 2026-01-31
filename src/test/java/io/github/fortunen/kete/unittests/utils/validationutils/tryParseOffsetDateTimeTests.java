package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

public class tryParseOffsetDateTimeTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("   ")).isEmpty();
	}

	@Test
	public void shouldParseIsoOffsetDateTime() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("2024-03-15T10:30:00+02:00"))
			.hasValue(OffsetDateTime.of(2024, 3, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2)));
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("  2024-03-15T10:30:00Z  "))
			.hasValue(OffsetDateTime.of(2024, 3, 15, 10, 30, 0, 0, ZoneOffset.UTC));
	}

	@Test
	public void shouldParseUtcOffset() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("2024-03-15T10:30:00Z"))
			.hasValue(OffsetDateTime.of(2024, 3, 15, 10, 30, 0, 0, ZoneOffset.UTC));
	}

	@Test
	public void shouldParseNegativeOffset() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("2024-03-15T10:30:00-05:00"))
			.hasValue(OffsetDateTime.of(2024, 3, 15, 10, 30, 0, 0, ZoneOffset.ofHours(-5)));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormat() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("2024-03-15 10:30:00")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForDateOnly() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("2024-03-15")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForNoOffset() {

		// act & assert

		assertThat(ValidationUtils.tryParseOffsetDateTime("2024-03-15T10:30:00")).isEmpty();
	}
}
