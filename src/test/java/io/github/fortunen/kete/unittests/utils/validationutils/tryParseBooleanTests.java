package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseBooleanTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("   ")).isEmpty();
	}

	@Test
	public void shouldParseTrue() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("true")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseTrueUppercase() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("TRUE")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseTrueMixedCase() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("TrUe")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseFalse() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("false")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldParseFalseUppercase() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("FALSE")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldParseFalseMixedCase() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("FaLsE")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("  true  ")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("yes")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForNumber() {

		// act & assert

		assertThat(ValidationUtils.tryParseBoolean("1")).isEmpty();
	}
}
