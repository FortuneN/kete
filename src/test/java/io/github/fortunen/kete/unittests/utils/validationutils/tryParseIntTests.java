package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseIntTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveInteger() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("123")).hasValue(123);
	}

	@Test
	public void shouldParseNegativeInteger() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("-456")).hasValue(-456);
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("0")).hasValue(0);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("  42  ")).hasValue(42);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("abc")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForFloat() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("3.14")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForOverflow() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("2147483648")).isEmpty();
	}

	@Test
	public void shouldParseMaxInt() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("2147483647")).hasValue(Integer.MAX_VALUE);
	}

	@Test
	public void shouldParseMinInt() {

		// act & assert

		assertThat(ValidationUtils.tryParseInt("-2147483648")).hasValue(Integer.MIN_VALUE);
	}
}
