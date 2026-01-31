package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseFloatTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveFloat() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("3.14")).hasValue(3.14f);
	}

	@Test
	public void shouldParseNegativeFloat() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("-2.71")).hasValue(-2.71f);
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("0")).hasValue(0.0f);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("  42.5  ")).hasValue(42.5f);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("abc")).isEmpty();
	}

	@Test
	public void shouldParseInteger() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("42")).hasValue(42.0f);
	}

	@Test
	public void shouldParseScientificNotation() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("1.5e5")).hasValue(1.5e5f);
	}

	@Test
	public void shouldParseInfinity() {

		// act & assert

		assertThat(ValidationUtils.tryParseFloat("Infinity")).hasValue(Float.POSITIVE_INFINITY);
	}
}
