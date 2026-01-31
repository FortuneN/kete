package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class isNullOrEmpty_ArrayTests {

	@Test
	public void shouldReturnTrueWhenNull() {

		// arrange

		String[] value = null;

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenEmptyArray() {

		// arrange

		var value = new String[0];

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenHasElements() {

		// arrange

		var value = new String[] { "a", "b", "c" };

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSingleElement() {

		// arrange

		var value = new String[] { "single" };

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenContainsNull() {

		// arrange

		var value = new String[] { null };

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldHandlePrimitiveArrayAsObjectArray() {

		// arrange

		var emptyArray = new Integer[0];
		var nonEmptyArray = new Integer[] { 1, 2, 3 };

		// act

		var emptyResult = ValidationUtils.isNullOrEmpty(emptyArray);
		var nonEmptyResult = ValidationUtils.isNullOrEmpty(nonEmptyArray);

		// assert

		assertThat(emptyResult).isTrue();
		assertThat(nonEmptyResult).isFalse();
	}

	@Test
	public void shouldHandleObjectArray() {

		// arrange

		var emptyArray = new Object[0];
		var nonEmptyArray = new Object[] { "a", 1, true };

		// act

		var emptyResult = ValidationUtils.isNullOrEmpty(emptyArray);
		var nonEmptyResult = ValidationUtils.isNullOrEmpty(nonEmptyArray);

		// assert

		assertThat(emptyResult).isTrue();
		assertThat(nonEmptyResult).isFalse();
	}

	@Test
	public void shouldHandleArrayWithOnlyNulls() {

		// arrange

		var value = new String[] { null, null, null };

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldHandleArrayWithEmptyStrings() {

		// arrange

		var value = new String[] { "", "", "" };

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldHandleMultidimensionalArray() {

		// arrange

		var emptyArray = new String[0][0];
		var nonEmptyArray = new String[][] { { "a" }, { "b" } };

		// act

		var emptyResult = ValidationUtils.isNullOrEmpty(emptyArray);
		var nonEmptyResult = ValidationUtils.isNullOrEmpty(nonEmptyArray);

		// assert

		assertThat(emptyResult).isTrue();
		assertThat(nonEmptyResult).isFalse();
	}
}
