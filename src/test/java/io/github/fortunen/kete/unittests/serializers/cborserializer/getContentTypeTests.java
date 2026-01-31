package io.github.fortunen.kete.unittests.serializers.cborserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.CborSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationCbor() {

		// arrange

		var serializer = new CborSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/cbor");
	}
}
