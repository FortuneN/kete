package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class isLocalDate_FormatterTests {

	@Test
	public void shouldReturnTrueForValidDateWithCustomFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.isLocalDate("15/01/2024", formatter);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUsDateFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

		// act

		var result = ValidationUtils.isLocalDate("01-15-2024", formatter);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForWrongFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.isLocalDate("2024-01-15", formatter);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.isLocalDate(null, formatter);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.isLocalDate("", formatter);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForCompactFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

		// act

		var result = ValidationUtils.isLocalDate("20240115", formatter);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidDate() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act

		var result = ValidationUtils.isLocalDate("32/01/2024", formatter);

		// assert

		assertThat(result).isFalse();
	}
}
