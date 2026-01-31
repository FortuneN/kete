package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class requireMatches_PatternTests {

	@Test
	public void shouldPassWhenStringMatchesPattern() {

		// arrange

		var value = "test123";
		var pattern = Pattern.compile("test\\d+");

		// act

		var result = ValidationUtils.requireMatches(value, pattern, "String must match pattern");

		// assert

		assertThat(result).isEqualTo("test123");
	}

	@Test
	public void shouldThrowWhenStringNull() {

		// arrange

		String value = null;
		var pattern = Pattern.compile("test");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, pattern, "String must match pattern");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("String must match pattern");
	}

	@Test
	public void shouldThrowWhenStringDoesNotMatch() {

		// arrange

		var value = "test";
		var pattern = Pattern.compile("\\d+");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, pattern, "String must match pattern");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("String must match pattern");
	}

	@Test
	public void shouldPassWhenEmailMatches() {

		// arrange

		var value = "test@example.com";
		var pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

		// act

		var result = ValidationUtils.requireMatches(value, pattern, "Must be valid email");

		// assert

		assertThat(result).isEqualTo("test@example.com");
	}

	@Test
	public void shouldThrowWhenEmailInvalid() {

		// arrange

		var value = "not-an-email";
		var pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, pattern, "Must be valid email");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Must be valid email");
	}

	@Test
	public void shouldPassWhenNumberMatches() {

		// arrange

		var value = "12345";
		var pattern = Pattern.compile("^\\d+$");

		// act

		var result = ValidationUtils.requireMatches(value, pattern, "Must be numeric");

		// assert

		assertThat(result).isEqualTo("12345");
	}

	@Test
	public void shouldPassWithComplexRegex() {

		// arrange

		var value = "ABC-123-XYZ";
		var pattern = Pattern.compile("^[A-Z]{3}-\\d{3}-[A-Z]{3}$");

		// act

		var result = ValidationUtils.requireMatches(value, pattern, "Must match format");

		// assert

		assertThat(result).isEqualTo("ABC-123-XYZ");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = "invalid";
		var pattern = Pattern.compile("\\d+");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, pattern, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = "test123";
		var pattern = Pattern.compile("test\\d+");
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireMatches(value, pattern, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandleCaseInsensitivePattern() {

		// arrange

		var value = "TeSt";
		var pattern = Pattern.compile("test", Pattern.CASE_INSENSITIVE);

		// act

		var result = ValidationUtils.requireMatches(value, pattern, "Must match pattern");

		// assert

		assertThat(result).isEqualTo("TeSt");
	}

	@Test
	public void shouldHandleMultilinePattern() {

		// arrange

		var value = "line1\nline2";
		var pattern = Pattern.compile("^line1$.*^line2$", Pattern.MULTILINE | Pattern.DOTALL);

		// act

		var result = ValidationUtils.requireMatches(value, pattern, "Must match multiline");

		// assert

		assertThat(result).isEqualTo("line1\nline2");
	}

	@Test
	public void shouldHandleWhitespacePattern() {

		// arrange

		var value = "test  spaces";
		var pattern = Pattern.compile("test\\s+spaces");

		// act

		var result = ValidationUtils.requireMatches(value, pattern, "Must have whitespace");

		// assert

		assertThat(result).isEqualTo("test  spaces");
	}

	@Test
	public void shouldThrowWhenNullWithSupplier() {

		// arrange

		String value = null;
		var pattern = Pattern.compile(".*");

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, pattern, () -> new IllegalStateException("Null value error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Null value error");
	}
}
