package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class tryParseLocalDateTimeTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("   ")).isEmpty();
	}

	@Test
	public void shouldParseIsoDateTime() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("2024-03-15T10:30:00"))
			.hasValue(LocalDateTime.of(2024, 3, 15, 10, 30, 0));
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("  2024-03-15T10:30:00  "))
			.hasValue(LocalDateTime.of(2024, 3, 15, 10, 30, 0));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormat() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("15/03/2024 10:30")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForDateOnly() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("2024-03-15")).isEmpty();
	}

	// With formatter

	@Test
	public void shouldReturnEmptyForNullFormatter() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("2024-03-15T10:30:00", null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForNullValueWithFormatter() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime(null, DateTimeFormatter.ISO_LOCAL_DATE_TIME)).isEmpty();
	}

	@Test
	public void shouldParseWithCustomFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("15/03/2024 10:30", formatter))
			.hasValue(LocalDateTime.of(2024, 3, 15, 10, 30, 0));
	}

	@Test
	public void shouldTrimWhitespaceWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("  15-03-2024 10:30:00  ", formatter))
			.hasValue(LocalDateTime.of(2024, 3, 15, 10, 30, 0));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormatWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDateTime("2024-03-15T10:30:00", formatter)).isEmpty();
	}
}
