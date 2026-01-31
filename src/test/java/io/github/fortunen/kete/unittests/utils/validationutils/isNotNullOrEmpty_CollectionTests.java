package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class isNotNullOrEmpty_CollectionTests {

	@Test
	public void shouldReturnFalseWhenNull() {

		// arrange

		Collection<String> value = null;

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenEmpty() {

		// arrange

		var value = new ArrayList<String>();

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenHasElements() {

		// arrange

		var value = List.of("a", "b", "c");

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenSingleElement() {

		// arrange

		var value = List.of("single");

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenContainsNull() {

		// arrange

		var value = new ArrayList<String>();
		value.add(null);

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenEmptyList() {

		// arrange

		var value = List.of();

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isFalse();
	}

	@Test
	public void shouldHandleModifiableCollection() {

		// arrange

		var value = new ArrayList<String>();

		// act

		var resultEmpty = ValidationUtils.isNotNullOrEmpty(value);
		value.add("test");
		var resultNotEmpty = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(resultEmpty).isFalse();
		assertThat(resultNotEmpty).isTrue();
	}

	@Test
	public void shouldHandleUnmodifiableCollection() {

		// arrange

		var emptyValue = List.of();
		var nonEmptyValue = List.of("a");

		// act

		var emptyResult = ValidationUtils.isNotNullOrEmpty(emptyValue);
		var nonEmptyResult = ValidationUtils.isNotNullOrEmpty(nonEmptyValue);

		// assert

		assertThat(emptyResult).isFalse();
		assertThat(nonEmptyResult).isTrue();
	}

	@Test
	public void shouldHandleCollectionWithOnlyNulls() {

		// arrange

		var value = new ArrayList<String>();
		value.add(null);
		value.add(null);
		value.add(null);

		// act

		var result = ValidationUtils.isNotNullOrEmpty(value);

		// assert

		assertThat(result).isTrue();
	}

	@Test
	public void shouldHandleSet() {

		// arrange

		var emptySet = new HashSet<String>();
		var nonEmptySet = Set.of("a", "b");

		// act

		var emptyResult = ValidationUtils.isNotNullOrEmpty(emptySet);
		var nonEmptyResult = ValidationUtils.isNotNullOrEmpty(nonEmptySet);

		// assert

		assertThat(emptyResult).isFalse();
		assertThat(nonEmptyResult).isTrue();
	}
}
