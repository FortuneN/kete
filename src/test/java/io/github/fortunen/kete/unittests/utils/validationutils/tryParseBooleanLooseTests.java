package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseBooleanLooseTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("   ")).isEmpty();
	}

	@Test
	public void shouldParseTrue() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("true")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseTrueUppercase() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("TRUE")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseOne() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("1")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseYes() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("yes")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseY() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("y")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseOn() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("on")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldParseFalse() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("false")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldParseFalseUppercase() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("FALSE")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("0")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldParseNo() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("no")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldParseN() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("n")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldParseOff() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("off")).hasValue(Boolean.FALSE);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("  yes  ")).hasValue(Boolean.TRUE);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBooleanLoose("maybe")).isEmpty();
	}
}
