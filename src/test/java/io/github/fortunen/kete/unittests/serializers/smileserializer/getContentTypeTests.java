package io.github.fortunen.kete.unittests.serializers.smileserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.SmileSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationXJacksonSmile() {

		// arrange

		var serializer = new SmileSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/x-jackson-smile");
	}
}
