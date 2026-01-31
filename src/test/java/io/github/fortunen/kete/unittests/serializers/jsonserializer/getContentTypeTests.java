package io.github.fortunen.kete.unittests.serializers.jsonserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.JsonSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationJson() {

		// arrange

		var serializer = new JsonSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/json");
	}
}
