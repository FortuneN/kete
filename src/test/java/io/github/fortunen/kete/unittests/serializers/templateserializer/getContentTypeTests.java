package io.github.fortunen.kete.unittests.serializers.templateserializer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.serializers.TemplateSerializer;

public class getContentTypeTests {

	@Test
	public void shouldReturnDefaultTextPlain() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		serializer.setConfiguration(new MapConfiguration(map));
		serializer.initialize();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("text/plain");
	}

	@Test
	public void shouldReturnCustomContentType() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		map.put("content-type", "application/json");
		serializer.setConfiguration(new MapConfiguration(map));
		serializer.initialize();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/json");
	}

	@Test
	public void shouldReturnTextHtmlContentType() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "<h1>test</h1>");
		map.put("content-type", "text/html");
		serializer.setConfiguration(new MapConfiguration(map));
		serializer.initialize();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("text/html");
	}
}
