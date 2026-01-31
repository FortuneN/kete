package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class tryParseLocalDateTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("   ")).isEmpty();
	}

	@Test
	public void shouldParseIsoDate() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("2024-03-15")).hasValue(LocalDate.of(2024, 3, 15));
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("  2024-03-15  ")).hasValue(LocalDate.of(2024, 3, 15));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormat() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("15/03/2024")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForInvalidDate() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("2024-02-30")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForPartialDate() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("2024-03")).isEmpty();
	}

	// With formatter

	@Test
	public void shouldReturnEmptyForNullFormatter() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("2024-03-15", null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForNullValueWithFormatter() {

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate(null, DateTimeFormatter.ISO_LOCAL_DATE)).isEmpty();
	}

	@Test
	public void shouldParseWithCustomFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("15/03/2024", formatter)).hasValue(LocalDate.of(2024, 3, 15));
	}

	@Test
	public void shouldTrimWhitespaceWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("  15-03-2024  ", formatter)).hasValue(LocalDate.of(2024, 3, 15));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormatWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act & assert

		assertThat(ValidationUtils.tryParseLocalDate("2024-03-15", formatter)).isEmpty();
	}
}
