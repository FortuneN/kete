package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseShortTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveShort() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("123")).hasValue((short) 123);
	}

	@Test
	public void shouldParseNegativeShort() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("-456")).hasValue((short) -456);
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("0")).hasValue((short) 0);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("  42  ")).hasValue((short) 42);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("abc")).isEmpty();
	}

	@Test
	public void shouldParseMaxShort() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("32767")).hasValue(Short.MAX_VALUE);
	}

	@Test
	public void shouldParseMinShort() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("-32768")).hasValue(Short.MIN_VALUE);
	}

	@Test
	public void shouldReturnEmptyForOverflow() {

		// act & assert

		assertThat(ValidationUtils.tryParseShort("32768")).isEmpty();
	}
}
