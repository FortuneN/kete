package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseLongTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveLong() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("123456789012")).hasValue(123456789012L);
	}

	@Test
	public void shouldParseNegativeLong() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("-123456789012")).hasValue(-123456789012L);
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("0")).hasValue(0L);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("  42  ")).hasValue(42L);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("abc")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForFloat() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("3.14")).isEmpty();
	}

	@Test
	public void shouldParseMaxLong() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("9223372036854775807")).hasValue(Long.MAX_VALUE);
	}

	@Test
	public void shouldParseMinLong() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("-9223372036854775808")).hasValue(Long.MIN_VALUE);
	}

	@Test
	public void shouldReturnEmptyForOverflow() {

		// act & assert

		assertThat(ValidationUtils.tryParseLong("9223372036854775808")).isEmpty();
	}
}
