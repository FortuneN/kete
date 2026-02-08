package io.github.fortunen.kete.unittests.utils.jsonutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class getStringTests {

	@Test
	public void shouldReturnValueWhenFieldExists() {

		// arrange

		var node = JsonUtils.parseJson("{\"name\":\"test\"}");

		// act

		var result = JsonUtils.getString(node, "name");

		// assert

		assertThat(result).isEqualTo("test");
	}

	@Test
	public void shouldReturnNullWhenFieldDoesNotExist() {

		// arrange

		var node = JsonUtils.parseJson("{\"name\":\"test\"}");

		// act

		var result = JsonUtils.getString(node, "missing");

		// assert

		assertThat(result).isNull();
	}

	@Test
	public void shouldReturnNullWhenFieldIsJsonNull() {

		// arrange

		var node = JsonUtils.parseJson("{\"name\":null}");

		// act

		var result = JsonUtils.getString(node, "name");

		// assert

		assertThat(result).isNull();
	}

	@Test
	public void shouldReturnNumericValueAsString() {

		// arrange

		var node = JsonUtils.parseJson("{\"count\":42}");

		// act

		var result = JsonUtils.getString(node, "count");

		// assert

		assertThat(result).isEqualTo("42");
	}

	@Test
	public void shouldReturnBooleanValueAsString() {

		// arrange

		var node = JsonUtils.parseJson("{\"active\":true}");

		// act

		var result = JsonUtils.getString(node, "active");

		// assert

		assertThat(result).isEqualTo("true");
	}

	@Test
	public void shouldReturnEmptyStringWhenFieldIsEmptyString() {

		// arrange

		var node = JsonUtils.parseJson("{\"name\":\"\"}");

		// act

		var result = JsonUtils.getString(node, "name");

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldThrowWhenNodeIsNull() {

		// act

		var thrown = catchThrowable(() -> JsonUtils.getString(null, "field"));

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("node is required");
	}

	@Test
	public void shouldThrowWhenFieldIsNull() {

		// arrange

		var node = JsonUtils.parseJson("{}");

		// act

		var thrown = catchThrowable(() -> JsonUtils.getString(node, null));

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("field is required");
	}

	@Test
	public void shouldThrowWhenFieldIsBlank() {

		// arrange

		var node = JsonUtils.parseJson("{}");

		// act

		var thrown = catchThrowable(() -> JsonUtils.getString(node, "  "));

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("field is required");
	}

	@Test
	public void shouldThrowWhenFieldIsEmpty() {

		// arrange

		var node = JsonUtils.parseJson("{}");

		// act

		var thrown = catchThrowable(() -> JsonUtils.getString(node, ""));

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("field is required");
	}
}
