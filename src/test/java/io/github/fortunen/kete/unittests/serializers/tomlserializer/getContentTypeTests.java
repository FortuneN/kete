package io.github.fortunen.kete.unittests.serializers.tomlserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.TomlSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationToml() {

		// arrange

		var serializer = new TomlSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/toml");
	}
}
