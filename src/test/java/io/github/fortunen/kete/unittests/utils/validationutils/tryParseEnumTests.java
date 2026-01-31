package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class tryParseEnumTests {

	@Test
	public void shouldReturnEmptyForNullEnumType() {

		// act & assert

		assertThat(ValidationUtils.<DayOfWeek>tryParseEnum(null, "MONDAY", false)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForNullValue() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, null, false)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyValue() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "", false)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankValue() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "   ", false)).isEmpty();
	}

	@Test
	public void shouldParseExactMatchCaseSensitive() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "MONDAY", false)).hasValue(DayOfWeek.MONDAY);
	}

	@Test
	public void shouldFailWrongCaseCaseSensitive() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "monday", false)).isEmpty();
	}

	@Test
	public void shouldParseIgnoreCase() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "monday", true)).hasValue(DayOfWeek.MONDAY);
	}

	@Test
	public void shouldParseMixedCaseIgnoreCase() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "MoNdAy", true)).hasValue(DayOfWeek.MONDAY);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "  MONDAY  ", false)).hasValue(DayOfWeek.MONDAY);
	}

	@Test
	public void shouldReturnEmptyForInvalidValue() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "INVALID", false)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForInvalidValueIgnoreCase() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(DayOfWeek.class, "invalid", true)).isEmpty();
	}

	@Test
	public void shouldParseTimeUnit() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(TimeUnit.class, "SECONDS", false)).hasValue(TimeUnit.SECONDS);
	}

	@Test
	public void shouldParseTimeUnitIgnoreCase() {

		// act & assert

		assertThat(ValidationUtils.tryParseEnum(TimeUnit.class, "milliseconds", true)).hasValue(TimeUnit.MILLISECONDS);
	}
}
