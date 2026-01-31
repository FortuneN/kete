package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isUuidTests {

	@Test
	public void shouldReturnTrueForValidUuid() {

		// act

		var result = ValidationUtils.isUuid("123e4567-e89b-12d3-a456-426614174000");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForUppercaseUuid() {

		// act

		var result = ValidationUtils.isUuid("123E4567-E89B-12D3-A456-426614174000");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMixedCaseUuid() {

		// act

		var result = ValidationUtils.isUuid("123e4567-E89B-12d3-A456-426614174000");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForNilUuid() {

		// act

		var result = ValidationUtils.isUuid("00000000-0000-0000-0000-000000000000");

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isUuid(null);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isUuid("");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidFormat() {

		// act

		var result = ValidationUtils.isUuid("not-a-uuid");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForUuidWithoutHyphens() {

		// act

		var result = ValidationUtils.isUuid("123e4567e89b12d3a456426614174000");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForTooShortUuid() {

		// act

		var result = ValidationUtils.isUuid("123e4567-e89b-12d3-a456");

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForInvalidCharacters() {

		// act

		var result = ValidationUtils.isUuid("123g4567-e89b-12d3-a456-426614174000");

		// assert

		assertThat(result).isFalse();
	}
}
