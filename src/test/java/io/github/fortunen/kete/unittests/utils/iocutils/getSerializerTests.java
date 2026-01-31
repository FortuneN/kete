package io.github.fortunen.kete.unittests.utils.iocutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.serializers.CborSerializer;
import io.github.fortunen.kete.serializers.CsvSerializer;
import io.github.fortunen.kete.serializers.JsonSerializer;
import io.github.fortunen.kete.serializers.PropertiesSerializer;
import io.github.fortunen.kete.serializers.SmileSerializer;
import io.github.fortunen.kete.serializers.TomlSerializer;
import io.github.fortunen.kete.serializers.XmlSerializer;
import io.github.fortunen.kete.serializers.YamlSerializer;
import io.github.fortunen.kete.utils.IocUtils;
import org.junit.jupiter.api.Test;

public class getSerializerTests {

	@Test
	public void shouldGetJsonSerializer() {

		// act

		var result = IocUtils.get("json", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return a JSON serializer")
			.isNotNull()
			.isInstanceOf(JsonSerializer.class);
	}

	@Test
	public void shouldGetXmlSerializer() {

		// act

		var result = IocUtils.get("xml", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return an XML serializer")
			.isNotNull()
			.isInstanceOf(XmlSerializer.class);
	}

	@Test
	public void shouldGetYamlSerializer() {

		// act

		var result = IocUtils.get("yaml", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return a YAML serializer")
			.isNotNull()
			.isInstanceOf(YamlSerializer.class);
	}

	@Test
	public void shouldGetCsvSerializer() {

		// act

		var result = IocUtils.get("csv", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return a CSV serializer")
			.isNotNull()
			.isInstanceOf(CsvSerializer.class);
	}

	@Test
	public void shouldGetCborSerializer() {

		// act

		var result = IocUtils.get("cbor", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return a CBOR serializer")
			.isNotNull()
			.isInstanceOf(CborSerializer.class);
	}

	@Test
	public void shouldGetPropertiesSerializer() {

		// act

		var result = IocUtils.get("properties", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return a Properties serializer")
			.isNotNull()
			.isInstanceOf(PropertiesSerializer.class);
	}

	@Test
	public void shouldGetSmileSerializer() {

		// act

		var result = IocUtils.get("smile", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return a Smile serializer")
			.isNotNull()
			.isInstanceOf(SmileSerializer.class);
	}

	@Test
	public void shouldGetTomlSerializer() {

		// act

		var result = IocUtils.get("toml", Serializer.class);

		// assert

		assertThat(result)
			.as("Should return a TOML serializer")
			.isNotNull()
			.isInstanceOf(TomlSerializer.class);
	}

	@Test
	public void shouldReturnSameInstanceForSingletonSerializers() {

		// act

		var first = IocUtils.get("json", Serializer.class);
		var second = IocUtils.get("json", Serializer.class);

		// assert

		assertThat(first)
			.as("Should return same instance for singleton serializers")
			.isSameAs(second);
	}
}
