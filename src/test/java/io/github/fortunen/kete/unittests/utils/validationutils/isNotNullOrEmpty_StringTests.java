package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNotNullOrEmpty_StringTests {

	@Test
	public void shouldReturnFalseWhenNull() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenEmpty() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenWhitespace() {

		// arrange

		var value = "   ";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenHasContent() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenSingleCharacter() {

		// arrange

		var value = "a";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenSingleSpace() {

		// arrange

		var value = " ";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenTab() {

		// arrange

		var value = "\t";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenNewline() {

		// arrange

		var value = "\n";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenSpecialCharacters() {

		// arrange

		var value = "!@#$";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenLeadingTrailingSpaces() {

		// arrange

		var value = "  test  ";

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldDifferentiateFromNotBlank() {

		// arrange

		var emptyString = "";
		var whitespaceString = "   ";

		// act

		var emptyResult = ValidationUtils.isNotNullOrEmpty(emptyString);
		var whitespaceResult = ValidationUtils.isNotNullOrEmpty(whitespaceString);

		// assert

		assertThat(emptyResult).isFalse();
		assertThat(whitespaceResult).isTrue();
	}
}
