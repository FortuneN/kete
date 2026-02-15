package io.github.fortunen.kete.unittests.utils.serializerutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.serializers.CborSerializer;
import io.github.fortunen.kete.serializers.CsvSerializer;
import io.github.fortunen.kete.serializers.JsonSerializer;
import io.github.fortunen.kete.serializers.PropertiesSerializer;
import io.github.fortunen.kete.serializers.SmileSerializer;
import io.github.fortunen.kete.serializers.TemplateSerializer;
import io.github.fortunen.kete.serializers.TomlSerializer;
import io.github.fortunen.kete.serializers.XmlSerializer;
import io.github.fortunen.kete.serializers.YamlSerializer;
import io.github.fortunen.kete.utils.SerializerUtils;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class createSerializerTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> SerializerUtils.createSerializer(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldThrowWhenKindIsMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> SerializerUtils.createSerializer(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("kind is required");
	}

	@Test
	void shouldThrowWhenKindIsEmpty() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> SerializerUtils.createSerializer(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("kind is required");
	}

	@Test
	void shouldThrowWhenKindIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "   ");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> SerializerUtils.createSerializer(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("kind is required");
	}

	@Test
	void shouldThrowWhenKindIsUnknown() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "unknown-serializer-type");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> SerializerUtils.createSerializer(config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("serializer kind 'unknown-serializer-type' not found");
	}

	@Test
	void shouldCreateJsonSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "json");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(JsonSerializer.class);
	}

	@Test
	void shouldCreateXmlSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "xml");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(XmlSerializer.class);
	}

	@Test
	void shouldCreateYamlSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "yaml");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(YamlSerializer.class);
	}

	@Test
	void shouldCreateCsvSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "csv");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(CsvSerializer.class);
	}

	@Test
	void shouldCreateCborSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "cbor");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(CborSerializer.class);
	}

	@Test
	void shouldCreateSmileSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "smile");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(SmileSerializer.class);
	}

	@Test
	void shouldCreateTomlSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "toml");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(TomlSerializer.class);
	}

	@Test
	void shouldCreatePropertiesSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "properties");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(PropertiesSerializer.class);
	}

	@Test
	void shouldCreateTemplateSerializer() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "template");
		map.put("template-file-text", "Hello ${userId}");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(TemplateSerializer.class);
	}

	@Test
	void shouldTrimKind() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "  json  ");
		var config = new MapConfiguration(map);

		// act

		var result = SerializerUtils.createSerializer(config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(JsonSerializer.class);
	}
}
