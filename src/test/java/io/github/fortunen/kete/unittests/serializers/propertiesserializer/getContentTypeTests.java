package io.github.fortunen.kete.unittests.serializers.propertiesserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.PropertiesSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnTextPlain() {

		// arrange

		var serializer = new PropertiesSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("text/plain");
	}
}
