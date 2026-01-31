package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNullOrEmpty_StringTests {

	@Test
	public void shouldReturnTrueWhenNull() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenEmpty() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenWhitespace() {

		// arrange

		var value = "   ";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenHasContent() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSingleCharacter() {

		// arrange

		var value = "a";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSingleSpace() {

		// arrange

		var value = " ";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenTab() {

		// arrange

		var value = "\t";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenNewline() {

		// arrange

		var value = "\n";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSpecialCharacters() {

		// arrange

		var value = "!@#$";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenLeadingTrailingSpaces() {

		// arrange

		var value = "  test  ";

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldDifferentiateFromBlank() {

		// arrange

		var emptyString = "";
		var whitespaceString = "   ";

		// act

		var emptyResult = ValidationUtils.isNullOrEmpty(emptyString);
		var whitespaceResult = ValidationUtils.isNullOrEmpty(whitespaceString);

		// assert

		assertThat(emptyResult).isTrue();
		assertThat(whitespaceResult).isFalse();
	}
}
