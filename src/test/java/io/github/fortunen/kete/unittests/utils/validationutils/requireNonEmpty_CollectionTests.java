package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

public class requireNonEmpty_CollectionTests {

	@Test
	public void shouldPassWhenCollectionHasElements() {

		// arrange

		var collection = List.of("a", "b", "c");

		// act

		var result = ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(3);
	}

	@Test
	public void shouldThrowWhenCollectionNull() {

		// arrange

		Collection<String> collection = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Collection must not be empty");
	}

	@Test
	public void shouldThrowWhenCollectionEmpty() {

		// arrange

		var collection = new ArrayList<String>();

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Collection must not be empty");
	}

	@Test
	public void shouldPassWhenCollectionHasSingleElement() {

		// arrange

		var collection = List.of("single");

		// act

		var result = ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void shouldPassWhenCollectionHasNullElement() {

		// arrange

		var collection = new ArrayList<String>();
		collection.add(null);

		// act

		var result = ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.iterator().next()).isNull();
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var collection = new ArrayList<String>();

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(collection, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var collection = List.of("test");
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireNonEmpty(collection, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldReturnSameCollectionInstance() {

		// arrange

		var collection = new ArrayList<String>();
		collection.add("test");

		// act

		var result = ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");

		// assert

		assertThat(result == collection).isTrue();
	}

	@Test
	public void shouldHandleModifiableCollection() {

		// arrange

		var collection = new ArrayList<String>();
		collection.add("initial");

		// act

		var result = ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");
		result.add("added");

		// assert

		assertThat(collection.size()).isEqualTo(2);
		assertThat(collection.contains("added")).isTrue();
	}

	@Test
	public void shouldHandleUnmodifiableCollection() {

		// arrange

		var collection = List.of("a", "b");

		// act

		var result = ValidationUtils.requireNonEmpty(collection, "Collection must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(2);
		var thrown = catchThrowable(() -> {
			result.add("c");
		});
		assertThat(thrown)
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void shouldThrowWhenNullWithSupplier() {

		// arrange

		List<String> collection = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(collection, () -> new IllegalStateException("Custom null error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom null error");
	}
}
