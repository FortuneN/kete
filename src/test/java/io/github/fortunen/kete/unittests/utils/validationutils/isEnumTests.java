package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isEnumTests {

	private enum TestEnum {
		VALUE_ONE,
		VALUE_TWO,
		VALUE_THREE
	}

	@Test
	public void shouldReturnTrueForExactMatchCaseSensitive() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, "VALUE_ONE", false);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForWrongCaseCaseSensitive() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, "value_one", false);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForWrongCaseCaseInsensitive() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, "value_one", true);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueForMixedCaseCaseInsensitive() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, "Value_One", true);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidValue() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, "INVALID_VALUE", false);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForNull() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, null, false);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseForEmptyString() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, "", false);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueForAllEnumValues() {

		// act & assert

		assertThat(ValidationUtils.isEnum(TestEnum.class, "VALUE_ONE", false)).isTrue();
		assertThat(ValidationUtils.isEnum(TestEnum.class, "VALUE_TWO", false)).isTrue();
		assertThat(ValidationUtils.isEnum(TestEnum.class, "VALUE_THREE", false)).isTrue();
	}

	@Test
	public void shouldReturnFalseForPartialMatch() {

		// act

		var result = ValidationUtils.isEnum(TestEnum.class, "VALUE", false);

		// assert

		assertThat(result).isFalse();
	}
}
