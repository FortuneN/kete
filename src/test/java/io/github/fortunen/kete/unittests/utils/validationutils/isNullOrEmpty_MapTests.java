package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class isNullOrEmpty_MapTests {

	@Test
	public void shouldReturnTrueWhenNull() {

		// arrange

		Map<String, String> value = null;

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenEmpty() {

		// arrange

		var value = new HashMap<String, String>();

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenHasEntries() {

		// arrange

		var value = Map.of("key1", "value1", "key2", "value2");

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSingleEntry() {

		// arrange

		var value = Map.of("key", "value");

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenContainsNullValue() {

		// arrange

		var value = new HashMap<String, String>();
		value.put("key", null);

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenEmptyMapOf() {

		// arrange

		var value = Map.of();

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldHandleModifiableMap() {

		// arrange

		var value = new HashMap<String, String>();

		// act

		var resultEmpty = ValidationUtils.isNullOrEmpty(value);
		value.put("key", "value");
		var resultNotEmpty = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(resultEmpty).isTrue();
		assertThat(resultNotEmpty).isFalse();
	}

	@Test
	public void shouldHandleUnmodifiableMap() {

		// arrange

		var emptyValue = Map.of();
		var nonEmptyValue = Map.of("key", "value");

		// act

		var emptyResult = ValidationUtils.isNullOrEmpty(emptyValue);
		var nonEmptyResult = ValidationUtils.isNullOrEmpty(nonEmptyValue);

		// assert

		assertThat(emptyResult).isTrue();
		assertThat(nonEmptyResult).isFalse();
	}

	@Test
	public void shouldHandleMapWithEmptyStringKeys() {

		// arrange

		var value = new HashMap<String, String>();
		value.put("", "value");

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldHandleMapWithEmptyStringValues() {

		// arrange

		var value = Map.of("key", "");

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}
}
