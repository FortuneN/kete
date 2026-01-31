package io.github.fortunen.kete.unittests.serializers.csvserializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.CsvSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnTextCsv() {

		// arrange

		var serializer = new CsvSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("text/csv");
	}
}
