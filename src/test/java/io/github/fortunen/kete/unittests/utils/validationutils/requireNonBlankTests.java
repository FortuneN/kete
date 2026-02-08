package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.io.IOException;
import java.sql.SQLException;
import javax.naming.NamingException;
import org.junit.jupiter.api.Test;

public class requireNonBlankTests {

	@Test
	public void shouldReturnValueWhenNotBlank() {

		// arrange

		var value = "valid text";

		// act

		var result = ValidationUtils.requireNonBlank(value, "cannot be blank");

		// assert

		assertThat(result).isEqualTo("valid text");
	}

	@Test
	public void shouldThrowWhenValueIsNull() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, "value is required");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("value is required");
	}

	@Test
	public void shouldThrowWhenValueIsEmpty() {

		// arrange

		var value = "";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, "cannot be empty");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("cannot be empty");
	}

	@Test
	public void shouldThrowWhenValueIsWhitespaceOnly() {

		// arrange

		var value = "   ";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, "cannot be whitespace");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("cannot be whitespace");
	}

	@Test
	public void shouldThrowWhenValueIsTabsAndNewlines() {

		// arrange

		var value = "\t\n\r";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, "whitespace only");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("whitespace only");
	}

	@Test
	public void shouldAcceptValueWithLeadingWhitespace() {

		// arrange

		var value = "  text";

		// act

		var result = ValidationUtils.requireNonBlank(value, "cannot be blank");

		// assert

		assertThat(result).isEqualTo("  text");
	}

	@Test
	public void shouldAcceptValueWithTrailingWhitespace() {

		// arrange

		var value = "text  ";

		// act

		var result = ValidationUtils.requireNonBlank(value, "cannot be blank");

		// assert

		assertThat(result).isEqualTo("text  ");
	}

	@Test
	public void shouldAcceptSingleCharacter() {

		// arrange

		var value = "a";

		// act

		var result = ValidationUtils.requireNonBlank(value, "cannot be blank");

		// assert

		assertThat(result).isEqualTo("a");
	}

	@Test
	public void shouldAcceptSpecialCharacters() {

		// arrange

		var value = "!@#$%";

		// act

		var result = ValidationUtils.requireNonBlank(value, "cannot be blank");

		// assert

		assertThat(result).isEqualTo("!@#$%");
	}

	@Test
	public void shouldThrowCustomExceptionFromSupplier() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, () -> new IllegalStateException("custom"));
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("custom");
	}

	@Test
	public void shouldNotInvokeSupplierWhenValueNotBlank() {

		// arrange

		var value = "present";
		var invoked = new boolean[]{false};

		// act

		var result = ValidationUtils.requireNonBlank(value, () -> {
			invoked[0] = true;
			return new IllegalStateException("should not be called");
		});

		// assert

		assertThat(result).isEqualTo("present");
		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldReturnValueWhenNonBreakingSpace() {

		// arrange
		// Apache StringUtils does not treat non-breaking space as blank
		var value = "\u00A0\u2003";

		// act

		var result = ValidationUtils.requireNonBlank(value, "unicode whitespace");

		// assert

		assertThat(result).isEqualTo("\u00A0\u2003");
	}

	// @SneakyThrows exception path tests

	@Test
	public void shouldThrowCheckedExceptionViaSneakyThrows() {

		// arrange

		String value = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, () -> new IOException("IO error"));
		});

		// assert - @SneakyThrows allows checked exception without declaring

		assertThat(thrown)
			.isInstanceOf(IOException.class)
			.hasMessage("IO error");
	}

	@Test
	public void shouldThrowCheckedExceptionForBlankValue() {

		// arrange

		var value = "   ";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, () -> new SQLException("DB error"));
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(SQLException.class)
			.hasMessage("DB error");
	}

	@Test
	public void shouldThrowCheckedExceptionForEmptyValue() {

		// arrange

		var value = "";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonBlank(value, () -> new NamingException("JNDI error"));
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(NamingException.class)
			.hasMessage("JNDI error");
	}
}
