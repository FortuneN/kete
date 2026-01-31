package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class tryParseLocalDateTime_FormatterTests {

	@Test
	public void shouldReturnValueForValidDateTimeWithCustomFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.tryParseLocalDateTime("15/01/2024 10:30:45", formatter);

		// assert

		assertThat(result).isPresent();
		assertThat(result.get().getYear()).isEqualTo(2024);
		assertThat(result.get().getMonthValue()).isEqualTo(1);
		assertThat(result.get().getDayOfMonth()).isEqualTo(15);
		assertThat(result.get().getHour()).isEqualTo(10);
		assertThat(result.get().getMinute()).isEqualTo(30);
		assertThat(result.get().getSecond()).isEqualTo(45);
	}

	@Test
	public void shouldReturnValueForUsDateTimeFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");

		// act

		var result = ValidationUtils.tryParseLocalDateTime("01-15-2024 10:30", formatter);

		// assert

		assertThat(result).isPresent();
		assertThat(result.get().getYear()).isEqualTo(2024);
		assertThat(result.get().getHour()).isEqualTo(10);
		assertThat(result.get().getMinute()).isEqualTo(30);
	}

	@Test
	public void shouldReturnEmptyForWrongFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.tryParseLocalDateTime("2024-01-15T10:30:00", formatter);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForNull() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.tryParseLocalDateTime(null, formatter);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.tryParseLocalDateTime("", formatter);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldReturnValueForCompactFormat() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		// act

		var result = ValidationUtils.tryParseLocalDateTime("20240115103045", formatter);

		// assert

		assertThat(result).isPresent();
		assertThat(result.get().getYear()).isEqualTo(2024);
	}

	@Test
	public void shouldReturnEmptyForInvalidTime() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// act

		var result = ValidationUtils.tryParseLocalDateTime("15/01/2024 25:00:00", formatter);

		// assert

		assertThat(result).isEmpty();
	}
}
