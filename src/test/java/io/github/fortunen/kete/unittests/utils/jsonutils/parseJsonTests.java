package io.github.fortunen.kete.unittests.utils.jsonutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class parseJsonTests {

	@Test
	public void shouldParseValidJsonObject() {

		// act

		var result = JsonUtils.parseJson("{\"key\":\"value\"}");

		// assert

		assertThat(result).isNotNull();
		assertThat(result.get("key").asText()).isEqualTo("value");
	}

	@Test
	public void shouldParseJsonWithMultipleFields() {

		// act

		var result = JsonUtils.parseJson("{\"name\":\"test\",\"age\":25,\"active\":true}");

		// assert

		assertThat(result.get("name").asText()).isEqualTo("test");
		assertThat(result.get("age").asInt()).isEqualTo(25);
		assertThat(result.get("active").asBoolean()).isTrue();
	}

	@Test
	public void shouldParseJsonWithNestedObjects() {

		// act

		var result = JsonUtils.parseJson("{\"outer\":{\"inner\":\"value\"}}");

		// assert

		assertThat(result.get("outer").get("inner").asText()).isEqualTo("value");
	}

	@Test
	public void shouldParseJsonArray() {

		// act

		var result = JsonUtils.parseJson("[1,2,3]");

		// assert

		assertThat(result.isArray()).isTrue();
		assertThat(result.size()).isEqualTo(3);
	}

	@Test
	public void shouldParseJsonWithNullValue() {

		// act

		var result = JsonUtils.parseJson("{\"key\":null}");

		// assert

		assertThat(result.get("key").isNull()).isTrue();
	}

	@Test
	public void shouldParseEmptyJsonObject() {

		// act

		var result = JsonUtils.parseJson("{}");

		// assert

		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	public void shouldThrowWhenJsonIsNull() {

		// act

		var thrown = catchThrowable(() -> JsonUtils.parseJson(null));

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("json is required");
	}

	@Test
	public void shouldThrowWhenJsonIsInvalid() {

		// act

		var thrown = catchThrowable(() -> JsonUtils.parseJson("not-json"));

		// assert

		assertThat(thrown).isNotNull();
	}

	@Test
	public void shouldReturnNullNodeWhenJsonIsEmpty() {

		// act

		var result = JsonUtils.parseJson("");

		// assert

		assertThat(result).isNotNull();
	}
}
