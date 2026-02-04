package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class tryParseDurationTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("   ")).isEmpty();
	}

	// ISO-8601 format

	@Test
	public void shouldParseIso8601Duration() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("PT15M")).hasValue(Duration.ofMinutes(15));
	}

	@Test
	public void shouldParseIso8601HoursAndMinutes() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("PT2H30M")).hasValue(Duration.ofHours(2).plusMinutes(30));
	}

	@Test
	public void shouldParseIso8601Days() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("P1D")).hasValue(Duration.ofDays(1));
	}

	// Digits only (seconds)

	@Test
	public void shouldParseDigitsAsSeconds() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("1500")).hasValue(Duration.ofSeconds(1500));
	}

	@Test
	public void shouldParseZeroDigits() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("0")).hasValue(Duration.ZERO);
	}

	// Nanoseconds

	@Test
	public void shouldParseNanoseconds() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("1000ns")).hasValue(Duration.ofNanos(1000));
	}

	// Microseconds

	@Test
	public void shouldParseMicroseconds() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("500us")).hasValue(Duration.ofNanos(500_000));
	}

	// Milliseconds with suffix

	@Test
	public void shouldParseMilliseconds() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("100ms")).hasValue(Duration.ofMillis(100));
	}

	// Seconds

	@Test
	public void shouldParseSeconds() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("30s")).hasValue(Duration.ofSeconds(30));
	}

	// Minutes

	@Test
	public void shouldParseMinutes() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("15m")).hasValue(Duration.ofMinutes(15));
	}

	// Hours

	@Test
	public void shouldParseHours() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("2h")).hasValue(Duration.ofHours(2));
	}

	// Days

	@Test
	public void shouldParseDays() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("7d")).hasValue(Duration.ofDays(7));
	}

	// Weeks

	@Test
	public void shouldParseWeeks() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("2w")).hasValue(Duration.ofDays(14));
	}

	// Years

	@Test
	public void shouldParseYears() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("1y")).hasValue(Duration.ofDays(365));
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("  30s  ")).hasValue(Duration.ofSeconds(30));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormat() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("invalid")).isEmpty();
	}

	@Test
	public void shouldParseUppercaseIso() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("PT1H")).hasValue(Duration.ofHours(1));
	}

	@Test
	public void shouldParseLowercaseIso() {

		// act & assert

		assertThat(ValidationUtils.tryParseDuration("pt1h")).hasValue(Duration.ofHours(1));
	}
}
