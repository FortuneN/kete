package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class tryParseLocalDate_FormatterTests {

	@Test
	public void shouldReturnValueForValidDateWithCustomFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.tryParseLocalDate("15/01/2024", formatter);

		// assert

		assertThat(result).isPresent();
		assertThat(result.get().getYear()).isEqualTo(2024);
		assertThat(result.get().getMonthValue()).isEqualTo(1);
		assertThat(result.get().getDayOfMonth()).isEqualTo(15);
	}

	@Test
	public void shouldReturnValueForUsDateFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

		// act

		var result = ValidationUtils.tryParseLocalDate("01-15-2024", formatter);

		// assert

		assertThat(result).isPresent();
		assertThat(result.get().getYear()).isEqualTo(2024);
		assertThat(result.get().getMonthValue()).isEqualTo(1);
		assertThat(result.get().getDayOfMonth()).isEqualTo(15);
	}

	@Test
	public void shouldReturnEmptyForWrongFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.tryParseLocalDate("2024-01-15", formatter);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForNull() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.tryParseLocalDate(null, formatter);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.tryParseLocalDate("", formatter);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldReturnValueForCompactFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

		// act

		var result = ValidationUtils.tryParseLocalDate("20240115", formatter);

		// assert

		assertThat(result).isPresent();
		assertThat(result.get().getYear()).isEqualTo(2024);
	}

	@Test
	public void shouldReturnEmptyForInvalidDate() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.tryParseLocalDate("32/01/2024", formatter);

		// assert

		assertThat(result).isEmpty();
	}
}
