package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireMatches_StringTests {

	@Test
	public void shouldPassWhenStringMatchesRegex() {

		// arrange

		var value = "test123";
		var regex = "test\\d+";

		// act

		var result = ValidationUtils.requireMatches(value, regex, "String must match regex");

		// assert

		assertThat(result).isEqualTo("test123");
	}

	@Test
	public void shouldThrowWhenStringNull() {

		// arrange

		String value = null;
		var regex = "test";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, regex, "String must match regex");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("String must match regex");
	}

	@Test
	public void shouldThrowWhenStringDoesNotMatch() {

		// arrange

		var value = "test";
		var regex = "\\d+";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, regex, "String must match regex");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("String must match regex");
	}

	@Test
	public void shouldPassWhenEmailMatches() {

		// arrange

		var value = "test@example.com";
		var regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

		// act

		var result = ValidationUtils.requireMatches(value, regex, "Must be valid email");

		// assert

		assertThat(result).isEqualTo("test@example.com");
	}

	@Test
	public void shouldThrowWhenEmailInvalid() {

		// arrange

		var value = "not-an-email";
		var regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, regex, "Must be valid email");
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
		var regex = "^\\d+$";

		// act

		var result = ValidationUtils.requireMatches(value, regex, "Must be numeric");

		// assert

		assertThat(result).isEqualTo("12345");
	}

	@Test
	public void shouldPassWithComplexRegex() {

		// arrange

		var value = "ABC-123-XYZ";
		var regex = "^[A-Z]{3}-\\d{3}-[A-Z]{3}$";

		// act

		var result = ValidationUtils.requireMatches(value, regex, "Must match format");

		// assert

		assertThat(result).isEqualTo("ABC-123-XYZ");
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var value = "invalid";
		var regex = "\\d+";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, regex, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var value = "test123";
		var regex = "test\\d+";
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireMatches(value, regex, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldHandlePhoneNumberPattern() {

		// arrange

		var value = "123-456-7890";
		var regex = "\\d{3}-\\d{3}-\\d{4}";

		// act

		var result = ValidationUtils.requireMatches(value, regex, "Must be valid phone");

		// assert

		assertThat(result).isEqualTo("123-456-7890");
	}

	@Test
	public void shouldHandleUuidPattern() {

		// arrange

		var value = "550e8400-e29b-41d4-a716-446655440000";
		var regex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

		// act

		var result = ValidationUtils.requireMatches(value, regex, "Must be valid UUID");

		// assert

		assertThat(result).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
	}

	@Test
	public void shouldThrowWhenNullWithSupplier() {

		// arrange

		String value = null;
		var regex = ".*";

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireMatches(value, regex, () -> new IllegalStateException("Null value error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Null value error");
	}
}
