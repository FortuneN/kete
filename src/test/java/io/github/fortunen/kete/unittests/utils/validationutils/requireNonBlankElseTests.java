package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonBlankElseTests {

	@Test
	public void shouldReturnValueWhenNonBlank() {

		// arrange

		var value = "test";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("test");
	}

	@Test
	public void shouldReturnDefaultWhenNull() {

		// arrange

		String value = null;
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnDefaultWhenEmpty() {

		// arrange

		var value = "";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnDefaultWhenWhitespace() {

		// arrange

		var value = "   ";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnValueWhenHasContent() {

		// arrange

		var value = "  test  ";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("  test  ");
	}

	@Test
	public void shouldReturnDefaultWhenTabs() {

		// arrange

		var value = "\t\t";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnDefaultWhenNewlines() {

		// arrange

		var value = "\n\r\n";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnNullDefaultWhenBlank() {

		// arrange

		var value = "";
		String defaultValue = null;

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo(null);
	}

	@Test
	public void shouldReturnValueWithSingleCharacter() {

		// arrange

		var value = "a";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("a");
	}

	@Test
	public void shouldReturnValueWhenNonBreakingSpace() {

		// arrange
		// Apache StringUtils does not treat non-breaking space as blank
		var value = "\u00A0\u2000\u3000";
		var defaultValue = "default";

		// act

		var result = ValidationUtils.requireNonBlankElse(value, defaultValue);

		// assert

		assertThat(result).isEqualTo("\u00A0\u2000\u3000");
	}
}
