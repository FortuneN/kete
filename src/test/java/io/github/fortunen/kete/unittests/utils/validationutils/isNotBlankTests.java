package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNotBlankTests {

	@Test
	public void shouldReturnFalseWhenNull() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenEmpty() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenWhitespace() {

		// arrange

		var value = "   ";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenHasContent() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenSingleCharacter() {

		// arrange

		var value = "a";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenTabs() {

		// arrange

		var value = "\t\t";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenNewlines() {

		// arrange

		var value = "\n\r\n";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenLeadingTrailingSpaces() {

		// arrange

		var value = "  test  ";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenSpecialCharacters() {

		// arrange

		var value = "!@#$";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenMixedWhitespace() {

		// arrange

		var value = " \t\n\r ";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenNonBreakingSpace() {

		// arrange
		// Apache StringUtils.isNotBlank() does not treat non-breaking space as whitespace
		var value = "\u00A0\u2000\u3000";

		// act

		var result = ValidationUtils.isNotBlank(value);

		// assert

		assertThat(result).isTrue();
	}
}
