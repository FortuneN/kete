package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class canParseTests {

	// isInt

	@Test
	public void shouldReturnTrueForValidInt() {

		// act & assert

		assertThat(ValidationUtils.isInt("123")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidInt() {

		// act & assert

		assertThat(ValidationUtils.isInt("abc")).isFalse();
	}

	// isLong

	@Test
	public void shouldReturnTrueForValidLong() {

		// act & assert

		assertThat(ValidationUtils.isLong("123456789012")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidLong() {

		// act & assert

		assertThat(ValidationUtils.isLong("abc")).isFalse();
	}

	// isShort

	@Test
	public void shouldReturnTrueForValidShort() {

		// act & assert

		assertThat(ValidationUtils.isShort("123")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidShort() {

		// act & assert

		assertThat(ValidationUtils.isShort("abc")).isFalse();
	}

	// isByte

	@Test
	public void shouldReturnTrueForValidByte() {

		// act & assert

		assertThat(ValidationUtils.isByte("123")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidByte() {

		// act & assert

		assertThat(ValidationUtils.isByte("abc")).isFalse();
	}

	// isDouble

	@Test
	public void shouldReturnTrueForValidDouble() {

		// act & assert

		assertThat(ValidationUtils.isDouble("3.14")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidDouble() {

		// act & assert

		assertThat(ValidationUtils.isDouble("abc")).isFalse();
	}

	// isFloat

	@Test
	public void shouldReturnTrueForValidFloat() {

		// act & assert

		assertThat(ValidationUtils.isFloat("3.14")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidFloat() {

		// act & assert

		assertThat(ValidationUtils.isFloat("abc")).isFalse();
	}

	// isDecimal

	@Test
	public void shouldReturnTrueForValidDecimal() {

		// act & assert

		assertThat(ValidationUtils.isDecimal("123.456")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidDecimal() {

		// act & assert

		assertThat(ValidationUtils.isDecimal("abc")).isFalse();
	}

	// isBigInteger

	@Test
	public void shouldReturnTrueForValidBigInteger() {

		// act & assert

		assertThat(ValidationUtils.isBigInteger("12345678901234567890")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidBigInteger() {

		// act & assert

		assertThat(ValidationUtils.isBigInteger("abc")).isFalse();
	}

	// isBoolean

	@Test
	public void shouldReturnTrueForBooleanTrue() {

		// act & assert

		assertThat(ValidationUtils.isBoolean("true")).isTrue();
	}

	@Test
	public void shouldReturnTrueForBooleanFalse() {

		// act & assert

		assertThat(ValidationUtils.isBoolean("false")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidBoolean() {

		// act & assert

		assertThat(ValidationUtils.isBoolean("yes")).isFalse();
	}

	// isBooleanLoose

	@Test
	public void shouldReturnTrueForBooleanLooseYes() {

		// act & assert

		assertThat(ValidationUtils.isBooleanLoose("yes")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidBooleanLoose() {

		// act & assert

		assertThat(ValidationUtils.isBooleanLoose("maybe")).isFalse();
	}

	// isLocalDate

	@Test
	public void shouldReturnTrueForValidLocalDate() {

		// act & assert

		assertThat(ValidationUtils.isLocalDate("2024-03-15")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidLocalDate() {

		// act & assert

		assertThat(ValidationUtils.isLocalDate("invalid")).isFalse();
	}

	@Test
	public void shouldReturnTrueForValidLocalDateWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act & assert

		assertThat(ValidationUtils.isLocalDate("15/03/2024", formatter)).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidLocalDateWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// act & assert

		assertThat(ValidationUtils.isLocalDate("2024-03-15", formatter)).isFalse();
	}

	// isLocalDateTime

	@Test
	public void shouldReturnTrueForValidLocalDateTime() {

		// act & assert

		assertThat(ValidationUtils.isLocalDateTime("2024-03-15T10:30:00")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidLocalDateTime() {

		// act & assert

		assertThat(ValidationUtils.isLocalDateTime("invalid")).isFalse();
	}

	@Test
	public void shouldReturnTrueForValidLocalDateTimeWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		// act & assert

		assertThat(ValidationUtils.isLocalDateTime("15/03/2024 10:30", formatter)).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidLocalDateTimeWithFormatter() {

		// arrange

		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		// act & assert

		assertThat(ValidationUtils.isLocalDateTime("2024-03-15T10:30:00", formatter)).isFalse();
	}

	// isInstant

	@Test
	public void shouldReturnTrueForValidInstant() {

		// act & assert

		assertThat(ValidationUtils.isInstant("2024-03-15T10:30:00Z")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidInstant() {

		// act & assert

		assertThat(ValidationUtils.isInstant("invalid")).isFalse();
	}

	// isOffsetDateTime

	@Test
	public void shouldReturnTrueForValidOffsetDateTime() {

		// act & assert

		assertThat(ValidationUtils.isOffsetDateTime("2024-03-15T10:30:00+02:00")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidOffsetDateTime() {

		// act & assert

		assertThat(ValidationUtils.isOffsetDateTime("invalid")).isFalse();
	}

	// isDuration

	@Test
	public void shouldReturnTrueForValidDuration() {

		// act & assert

		assertThat(ValidationUtils.isDuration("PT15M")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidDuration() {

		// act & assert

		assertThat(ValidationUtils.isDuration("invalid")).isFalse();
	}

	// isUuid

	@Test
	public void shouldReturnTrueForValidUuid() {

		// act & assert

		assertThat(ValidationUtils.isUuid("550e8400-e29b-41d4-a716-446655440000")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidUuid() {

		// act & assert

		assertThat(ValidationUtils.isUuid("invalid")).isFalse();
	}

	// isEnum

	@Test
	public void shouldReturnTrueForValidEnum() {

		// act & assert

		assertThat(ValidationUtils.isEnum(DayOfWeek.class, "MONDAY", false)).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidEnum() {

		// act & assert

		assertThat(ValidationUtils.isEnum(DayOfWeek.class, "INVALID", false)).isFalse();
	}

	@Test
	public void shouldReturnTrueForEnumIgnoreCase() {

		// act & assert

		assertThat(ValidationUtils.isEnum(DayOfWeek.class, "monday", true)).isTrue();
	}
}
