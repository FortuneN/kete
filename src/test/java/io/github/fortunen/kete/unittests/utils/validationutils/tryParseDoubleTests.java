package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseDoubleTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveDouble() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("3.14159")).hasValue(3.14159);
	}

	@Test
	public void shouldParseNegativeDouble() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("-2.71828")).hasValue(-2.71828);
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("0")).hasValue(0.0);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("  42.5  ")).hasValue(42.5);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("abc")).isEmpty();
	}

	@Test
	public void shouldParseInteger() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("42")).hasValue(42.0);
	}

	@Test
	public void shouldParseScientificNotation() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("1.5e10")).hasValue(1.5e10);
	}

	@Test
	public void shouldParseMaxDouble() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble(String.valueOf(Double.MAX_VALUE))).hasValue(Double.MAX_VALUE);
	}

	@Test
	public void shouldParseInfinity() {

		// act & assert

		assertThat(ValidationUtils.tryParseDouble("Infinity")).hasValue(Double.POSITIVE_INFINITY);
	}
}
