package io.github.fortunen.kete.unittests.serializers.multipartformserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.MultipartFormSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnMultipartFormDataWithBoundary() {

		// arrange

		var serializer = new MultipartFormSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("multipart/form-data; boundary=kete-boundary");
	}
}
