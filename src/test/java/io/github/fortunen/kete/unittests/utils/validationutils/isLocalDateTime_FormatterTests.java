package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class isLocalDateTime_FormatterTests {

	@Test
	public void shouldReturnTrueForValidDateTimeWithCustomFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.isLocalDateTime("15/01/2024 10:30:00", formatter);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUsDateTimeFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");

		// act

		var result = ValidationUtils.isLocalDateTime("01-15-2024 10:30", formatter);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForWrongFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.isLocalDateTime("2024-01-15T10:30:00", formatter);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.isLocalDateTime(null, formatter);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.isLocalDateTime("", formatter);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForCompactFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		// act

		var result = ValidationUtils.isLocalDateTime("20240115103000", formatter);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidTime() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.isLocalDateTime("15/01/2024 25:00:00", formatter);

		// assert

		assertThat(result).isFalse();
	}
}
