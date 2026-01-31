package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class tryParseDecimalTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveDecimal() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("123.456")).hasValue(new BigDecimal("123.456"));
	}

	@Test
	public void shouldParseNegativeDecimal() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("-456.789")).hasValue(new BigDecimal("-456.789"));
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("0")).hasValue(BigDecimal.ZERO);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("  42.5  ")).hasValue(new BigDecimal("42.5"));
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("abc")).isEmpty();
	}

	@Test
	public void shouldParseInteger() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("42")).hasValue(new BigDecimal("42"));
	}

	@Test
	public void shouldParseScientificNotation() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("1.5E+10")).hasValue(new BigDecimal("1.5E+10"));
	}

	@Test
	public void shouldParseLargeNumber() {

		// act & assert

		assertThat(ValidationUtils.tryParseDecimal("12345678901234567890.12345678901234567890"))
			.hasValue(new BigDecimal("12345678901234567890.12345678901234567890"));
	}
}
