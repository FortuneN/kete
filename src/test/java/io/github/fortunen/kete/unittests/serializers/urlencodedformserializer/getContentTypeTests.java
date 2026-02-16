package io.github.fortunen.kete.unittests.serializers.urlencodedformserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.UrlEncodedFormSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationXWwwFormUrlencoded() {

		// arrange

		var serializer = new UrlEncodedFormSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/x-www-form-urlencoded");
	}
}
