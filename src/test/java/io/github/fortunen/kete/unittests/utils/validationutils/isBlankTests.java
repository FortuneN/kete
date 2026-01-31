package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isBlankTests {

	@Test
	public void shouldReturnTrueWhenNull() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenEmpty() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenWhitespace() {

		// arrange

		var value = "   ";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenHasContent() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSingleCharacter() {

		// arrange

		var value = "a";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenTabs() {

		// arrange

		var value = "\t\t";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenNewlines() {

		// arrange

		var value = "\n\r\n";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenLeadingTrailingSpaces() {

		// arrange

		var value = "  test  ";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSpecialCharacters() {

		// arrange

		var value = "!@#$";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenMixedWhitespace() {

		// arrange

		var value = " \t\n\r ";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenNonBreakingSpace() {

		// arrange
		// Apache StringUtils.isBlank() does not treat non-breaking space as whitespace
		var value = "\u00A0\u2000\u3000";

		// act

		var result = ValidationUtils.isBlank(value);

		// assert

		assertThat(result).isFalse();
	}
}
