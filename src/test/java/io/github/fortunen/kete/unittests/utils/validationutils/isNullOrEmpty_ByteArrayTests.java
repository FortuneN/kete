package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNullOrEmpty_ByteArrayTests {

	@Test
	public void shouldReturnTrueForNull() {

		// act & assert

		assertThat(ValidationUtils.isNullOrEmpty((byte[]) null)).isTrue();
	}

	@Test
	public void shouldReturnTrueForEmptyArray() {

		// act & assert

		assertThat(ValidationUtils.isNullOrEmpty(new byte[0])).isTrue();
	}

	@Test
	public void shouldReturnFalseForNonEmptyArray() {

		// act & assert

		assertThat(ValidationUtils.isNullOrEmpty(new byte[] { 1 })).isFalse();
	}

	@Test
	public void shouldReturnFalseForArrayWithMultipleElements() {

		// act & assert

		assertThat(ValidationUtils.isNullOrEmpty(new byte[] { 1, 2, 3 })).isFalse();
	}

	@Test
	public void shouldReturnFalseForArrayWithZeros() {

		// act & assert

		assertThat(ValidationUtils.isNullOrEmpty(new byte[] { 0, 0, 0 })).isFalse();
	}
}
