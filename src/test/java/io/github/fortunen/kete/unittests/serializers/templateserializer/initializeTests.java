package io.github.fortunen.kete.unittests.serializers.templateserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.fortunen.kete.serializers.TemplateSerializer;
import io.github.fortunen.kete.utils.Base64Utils;

public class initializeTests {

	@Test
	public void shouldInitializeWithTemplateFileText() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "Hello ${userId}");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getTemplate()).isEqualTo("Hello ${userId}");
		assertThat(serializer.getContentType()).isEqualTo("text/plain");
		assertThat(serializer.getVariableEscape()).isEqualTo("none");
	}

	@Test
	public void shouldInitializeWithTemplateFileBase64() {

		// arrange

		var serializer = new TemplateSerializer();
		var template = "Hello ${userId}";
		var base64 = Base64Utils.encode(template.getBytes(StandardCharsets.UTF_8));
		var map = new HashMap<String, Object>();
		map.put("template-file-base64", base64);
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getTemplate()).isEqualTo("Hello ${userId}");
	}

	@Test
	public void shouldInitializeWithTemplateFilePath(@TempDir Path tempDir) throws Exception {

		// arrange

		var templateFile = tempDir.resolve("template.txt");
		Files.writeString(templateFile, "Hello ${userId} from file");

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-path", templateFile.toString());
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getTemplate()).isEqualTo("Hello ${userId} from file");
	}

	@Test
	public void shouldUseCustomContentType() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		map.put("content-type", "application/json");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void shouldUseCustomVariableEscape() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		map.put("variable-escape", "json");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getVariableEscape()).isEqualTo("json");
	}

	@Test
	public void shouldThrowWhenConfigurationIsNull() {

		// arrange

		var serializer = new TemplateSerializer();

		// act

		var thrown = catchThrowable(() -> serializer.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	public void shouldThrowWhenNoTemplateSourceIsSet() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> serializer.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("exactly one of");
	}

	@Test
	public void shouldThrowWhenMultipleTemplateSourcesAreSet() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "inline template");
		map.put("template-file-base64", "aW5saW5l");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> serializer.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("exactly one of");
	}

	@Test
	public void shouldThrowWhenAllThreeTemplateSourcesAreSet() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "inline");
		map.put("template-file-base64", "aW5saW5l");
		map.put("template-file-path", "/some/path");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> serializer.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("exactly one of");
	}

	@Test
	public void shouldTrimContentType() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		map.put("content-type", "  text/html  ");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getContentType()).isEqualTo("text/html");
	}

	@Test
	public void shouldNormalizeVariableEscapeToLowerCase() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		map.put("variable-escape", "JSON");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getVariableEscape()).isEqualTo("json");
	}

	@Test
	public void shouldDefaultContentTypeToTextPlain() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getContentType()).isEqualTo("text/plain");
	}

	@Test
	public void shouldDefaultVariableEscapeToNone() {

		// arrange

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", "test");
		serializer.setConfiguration(new MapConfiguration(map));

		// act

		serializer.initialize();

		// assert

		assertThat(serializer.getVariableEscape()).isEqualTo("none");
	}
}
