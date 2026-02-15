package io.github.fortunen.kete.unittests.serializers.avroserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.AvroSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationAvro() {

		// arrange

		var serializer = new AvroSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/avro");
	}
}
