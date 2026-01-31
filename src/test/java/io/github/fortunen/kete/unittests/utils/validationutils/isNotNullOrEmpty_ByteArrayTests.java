package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNotNullOrEmpty_ByteArrayTests {

	@Test
	public void shouldReturnFalseForNull() {

		// act & assert

		assertThat(ValidationUtils.isNotNullOrEmpty((byte[]) null)).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyArray() {

		// act & assert

		assertThat(ValidationUtils.isNotNullOrEmpty(new byte[0])).isFalse();
	}

	@Test
	public void shouldReturnTrueForNonEmptyArray() {

		// act & assert

		assertThat(ValidationUtils.isNotNullOrEmpty(new byte[] { 1 })).isTrue();
	}

	@Test
	public void shouldReturnTrueForArrayWithMultipleElements() {

		// act & assert

		assertThat(ValidationUtils.isNotNullOrEmpty(new byte[] { 1, 2, 3 })).isTrue();
	}

	@Test
	public void shouldReturnTrueForArrayWithZeros() {

		// act & assert

		assertThat(ValidationUtils.isNotNullOrEmpty(new byte[] { 0, 0, 0 })).isTrue();
	}
}
