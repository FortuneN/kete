package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonEmpty_StringTests {

	@Test
	public void shouldPassWhenStringHasContent() {

		// arrange

		var value = "test";

		// act

		var result = ValidationUtils.requireNonEmpty(value, "String must not be empty");

		// assert

		assertThat(result).isEqualTo("test");
	}

	@Test
	public void shouldThrowWhenStringNull() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(value, "String must not be empty");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("String must not be empty");
	}

	@Test
	public void shouldThrowWhenStringEmpty() {

		// arrange

		var value = "";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(value, "String must not be empty");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("String must not be empty");
	}

	@Test
	public void shouldPassWhenStringHasWhitespace() {

		// arrange

		var value = "   ";

		// act

		var result = ValidationUtils.requireNonEmpty(value, "String must not be empty");

		// assert

		assertThat(result).isEqualTo("   ");
	}

	@Test
	public void shouldPassWhenStringHasSingleCharacter() {

		// arrange

		var value = "a";

		// act

		var result = ValidationUtils.requireNonEmpty(value, "String must not be empty");

		// assert

		assertThat(result).isEqualTo("a");
	}

	@Test
	public void shouldPassWhenStringHasSpecialCharacters() {

		// arrange

		var value = "!@#$%";

		// act

		var result = ValidationUtils.requireNonEmpty(value, "String must not be empty");

		// assert

		assertThat(result).isEqualTo("!@#$%");
	}

	@Test
	public void shouldPassWhenStringHasNewlines() {

		// arrange

		var value = "\n\r";

		// act

		var result = ValidationUtils.requireNonEmpty(value, "String must not be empty");

		// assert

		assertThat(result).isEqualTo("\n\r");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = "";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(value, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = "test";
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireNonEmpty(value, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldDifferentiateFromBlankCheck() {

		// arrange

		var whitespaceOnly = "   ";
		var empty = "";

		// act

		var result = ValidationUtils.requireNonEmpty(whitespaceOnly, "String must not be empty");

		// assert

		assertThat(result).isEqualTo("   ");

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(empty, "String must not be empty");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("String must not be empty");
	}

	@Test
	public void shouldThrowWhenNullWithSupplier() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(value, () -> new IllegalStateException("Custom null error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom null error");
	}
}
