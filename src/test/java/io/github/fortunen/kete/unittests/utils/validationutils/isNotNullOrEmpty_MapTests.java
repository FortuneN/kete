package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class isNotNullOrEmpty_MapTests {

	@Test
	public void shouldReturnFalseWhenNull() {

		// arrange

		Map<String, String> value = null;

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenEmpty() {

		// arrange

		var value = new HashMap<String, String>();

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenHasEntries() {

		// arrange

		var value = Map.of("key1", "value1", "key2", "value2");

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenSingleEntry() {

		// arrange

		var value = Map.of("key", "value");

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenContainsNullValue() {

		// arrange

		var value = new HashMap<String, String>();
		value.put("key", null);

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenEmptyMapOf() {

		// arrange

		var value = Map.of();

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldHandleModifiableMap() {

		// arrange

		var value = new HashMap<String, String>();

		// act

		var resultEmpty = ValidationUtils.isNotNullOrEmpty(value);
		value.put("key", "value");
		var resultNotEmpty = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(resultEmpty).isFalse();
		assertThat(resultNotEmpty).isTrue();
	}

	@Test
	public void shouldHandleUnmodifiableMap() {

		// arrange

		var emptyValue = Map.of();
		var nonEmptyValue = Map.of("key", "value");

		// act

		var emptyResult = ValidationUtils.isNotNullOrEmpty(emptyValue);
		var nonEmptyResult = ValidationUtils.isNotNullOrEmpty(nonEmptyValue);

		// assert

		assertThat(emptyResult).isFalse();
		assertThat(nonEmptyResult).isTrue();
	}

	@Test
	public void shouldHandleMapWithEmptyStringKeys() {

		// arrange

		var value = new HashMap<String, String>();
		value.put("", "value");

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldHandleMapWithEmptyStringValues() {

		// arrange

		var value = Map.of("key", "");

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}
}
