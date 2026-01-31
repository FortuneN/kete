package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class requireNonEmpty_MapTests {

	@Test
	public void shouldPassWhenMapHasEntries() {

		// arrange

		var map = Map.of("key1", "value1", "key2", "value2");

		// act

		var result = ValidationUtils.requireNonEmpty(map, "Map must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(2);
	}

	@Test
	public void shouldThrowWhenMapNull() {

		// arrange

		Map<String, String> map = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(map, "Map must not be empty");
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Map must not be empty");
	}

	@Test
	public void shouldThrowWhenMapEmpty() {

		// arrange

		var map = new HashMap<String, String>();

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(map, "Map must not be empty");
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Map must not be empty");
	}

	@Test
	public void shouldPassWhenMapHasSingleEntry() {

		// arrange

		var map = Map.of("key", "value");

		// act

		var result = ValidationUtils.requireNonEmpty(map, "Map must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void shouldPassWhenMapHasNullValue() {

		// arrange

		var map = new HashMap<String, String>();
		map.put("key", null);

		// act

		var result = ValidationUtils.requireNonEmpty(map, "Map must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get("key")).isNull();
	}

	@Test
	public void shouldThrowWithCustomException() {

		// arrange

		var map = new HashMap<String, String>();

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(map, () -> new IllegalStateException("Custom error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom error");
	}

	@Test
	public void shouldInvokeSupplierOnlyOnError() {

		// arrange

		var map = Map.of("key", "value");
		var invoked = new boolean[] { false };

		// act

		ValidationUtils.requireNonEmpty(map, () -> {
			invoked[0] = true;
			return new IllegalStateException("Should not be thrown");
		});

		// assert

		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldReturnSameMapInstance() {

		// arrange

		var map = new HashMap<String, String>();
		map.put("key", "value");

		// act

		var result = ValidationUtils.requireNonEmpty(map, "Map must not be empty");

		// assert

		assertThat(result == map).isTrue();
	}

	@Test
	public void shouldHandleModifiableMap() {

		// arrange

		var map = new HashMap<String, String>();
		map.put("initial", "value");

		// act

		var result = ValidationUtils.requireNonEmpty(map, "Map must not be empty");
		result.put("added", "new");

		// assert

		assertThat(map.size()).isEqualTo(2);
		assertThat(map.containsKey("added")).isTrue();
	}

	@Test
	public void shouldHandleUnmodifiableMap() {

		// arrange

		var map = Map.of("key", "value");

		// act

		var result = ValidationUtils.requireNonEmpty(map, "Map must not be empty");

		// assert

		assertThat(result.size()).isEqualTo(1);
		var thrown = catchThrowable(() -> {
			result.put("new", "value");
		});
		assertThat(thrown)
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void shouldThrowWhenNullWithSupplier() {

		// arrange

		Map<String, String> map = null;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireNonEmpty(map, () -> new IllegalStateException("Custom null error"));
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Custom null error");
	}
}
