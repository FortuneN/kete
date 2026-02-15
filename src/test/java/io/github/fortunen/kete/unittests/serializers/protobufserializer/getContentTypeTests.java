package io.github.fortunen.kete.unittests.serializers.protobufserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.ProtobufSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationXProtobuf() {

		// arrange

		var serializer = new ProtobufSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/x-protobuf");
	}
}
