package io.github.fortunen.kete.unittests.utils.jsonutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class createObjectNodeTests {

	@Test
	public void shouldReturnNonNullObjectNode() {

		// act

		var result = JsonUtils.createObjectNode();

		// assert

		assertThat(result).isNotNull();
	}

	@Test
	public void shouldReturnEmptyObjectNode() {

		// act

		var result = JsonUtils.createObjectNode();

		// assert

		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	public void shouldReturnMutableObjectNode() {

		// act

		var result = JsonUtils.createObjectNode();
		result.put("key", "value");

		// assert

		assertThat(result.get("key").asText()).isEqualTo("value");
	}

	@Test
	public void shouldReturnDistinctInstancesPerCall() {

		// act

		var first = JsonUtils.createObjectNode();
		var second = JsonUtils.createObjectNode();

		// assert

		assertThat(first).isNotSameAs(second);
	}
}
