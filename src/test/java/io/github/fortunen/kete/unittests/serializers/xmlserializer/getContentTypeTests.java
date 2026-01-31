package io.github.fortunen.kete.unittests.serializers.xmlserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.XmlSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnApplicationXml() {

		// arrange

		var serializer = new XmlSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/xml");
	}
}
