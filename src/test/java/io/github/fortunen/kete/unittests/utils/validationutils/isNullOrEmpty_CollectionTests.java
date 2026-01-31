package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class isNullOrEmpty_CollectionTests {

	@Test
	public void shouldReturnTrueWhenNull() {

		// arrange

		Collection<String> value = null;

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenEmpty() {

		// arrange

		var value = new ArrayList<String>();

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenHasElements() {

		// arrange

		var value = List.of("a", "b", "c");

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenSingleElement() {

		// arrange

		var value = List.of("single");

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenContainsNull() {

		// arrange

		var value = new ArrayList<String>();
		value.add(null);

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenEmptyList() {

		// arrange

		var value = List.of();

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldHandleModifiableCollection() {

		// arrange

		var value = new ArrayList<String>();

		// act

		var resultEmpty = ValidationUtils.isNullOrEmpty(value);
		value.add("test");
		var resultNotEmpty = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(resultEmpty).isTrue();
		assertThat(resultNotEmpty).isFalse();
	}

	@Test
	public void shouldHandleUnmodifiableCollection() {

		// arrange

		var emptyValue = List.of();
		var nonEmptyValue = List.of("a");

		// act

		var emptyResult = ValidationUtils.isNullOrEmpty(emptyValue);
		var nonEmptyResult = ValidationUtils.isNullOrEmpty(nonEmptyValue);

		// assert

		assertThat(emptyResult).isTrue();
		assertThat(nonEmptyResult).isFalse();
	}

	@Test
	public void shouldHandleCollectionWithOnlyNulls() {

		// arrange

		var value = new ArrayList<String>();
		value.add(null);
		value.add(null);
		value.add(null);

		// act

		var result = ValidationUtils.isNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldHandleSet() {

		// arrange

		var emptySet = new HashSet<String>();
		var nonEmptySet = Set.of("a", "b");

		// act

		var emptyResult = ValidationUtils.isNullOrEmpty(emptySet);
		var nonEmptyResult = ValidationUtils.isNullOrEmpty(nonEmptySet);

		// assert

		assertThat(emptyResult).isTrue();
		assertThat(nonEmptyResult).isFalse();
	}
}
