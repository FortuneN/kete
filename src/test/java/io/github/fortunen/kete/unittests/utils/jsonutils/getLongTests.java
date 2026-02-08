package io.github.fortunen.kete.unittests.utils.jsonutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class getLongTests {

	@Test
	public void shouldReturnValueWhenFieldExists() {

		// arrange

		var node = JsonUtils.parseJson("{\"exp\":1234567890}");

		// act

		var result = JsonUtils.getLong(node, "exp");

		// assert

		assertThat(result).isEqualTo(1234567890L);
	}

	@Test
	public void shouldReturnNullWhenFieldDoesNotExist() {

		// arrange

		var node = JsonUtils.parseJson("{\"name\":\"test\"}");

		// act

		var result = JsonUtils.getLong(node, "missing");

		// assert

		assertThat(result).isNull();
	}

	@Test
	public void shouldReturnNullWhenFieldIsJsonNull() {

		// arrange

		var node = JsonUtils.parseJson("{\"exp\":null}");

		// act

		var result = JsonUtils.getLong(node, "exp");

		// assert

		assertThat(result).isNull();
	}

	@Test
	public void shouldReturnZero() {

		// arrange

		var node = JsonUtils.parseJson("{\"exp\":0}");

		// act

		var result = JsonUtils.getLong(node, "exp");

		// assert

		assertThat(result).isEqualTo(0L);
	}

	@Test
	public void shouldReturnNegativeValue() {

		// arrange

		var node = JsonUtils.parseJson("{\"exp\":-100}");

		// act

		var result = JsonUtils.getLong(node, "exp");

		// assert

		assertThat(result).isEqualTo(-100L);
	}

	@Test
	public void shouldReturnLargeValue() {

		// arrange

		var node = JsonUtils.parseJson("{\"exp\":9999999999999}");

		// act

		var result = JsonUtils.getLong(node, "exp");

		// assert

		assertThat(result).isEqualTo(9999999999999L);
	}

	@Test
	public void shouldThrowWhenNodeIsNull() {

		// act

		var thrown = catchThrowable(() -> JsonUtils.getLong(null, "field"));

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

		var thrown = catchThrowable(() -> JsonUtils.getLong(node, null));

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

		var thrown = catchThrowable(() -> JsonUtils.getLong(node, "  "));

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

		var thrown = catchThrowable(() -> JsonUtils.getLong(node, ""));

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("field is required");
	}
}
