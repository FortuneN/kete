package io.github.fortunen.kete.unittests.serializers.yamlserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.YamlSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationYaml() {

		// arrange

		var serializer = new YamlSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/yaml");
	}
}
