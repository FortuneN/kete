package io.github.fortunen.kete.unittests.utils.configurationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.ConfigurationUtils;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class getSubSetTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> ConfigurationUtils.getSubSet(null, "prefix"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldThrowWhenPrefixIsNull() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act & assert

		assertThatThrownBy(() -> ConfigurationUtils.getSubSet(config, null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("prefix is required");
	}

	@Test
	void shouldReturnEmptyConfigurationForNonExistentPrefix() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("key1", "value1");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getSubSet(config, "nonexistent");

		// assert

		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void shouldReturnSubSetWithPrefixRemoved() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("destination.host", "localhost");
		map.put("destination.port", "8080");
		map.put("other.key", "value");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getSubSet(config, "destination");

		// assert

		assertThat(result.getString("host")).isEqualTo("localhost");
		assertThat(result.getString("port")).isEqualTo("8080");
		assertThat(result.containsKey("other.key")).isFalse();
	}

	@Test
	void shouldTrimKeysAndValues() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("destination.host", "  localhost  ");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getSubSet(config, "destination");

		// assert

		assertThat(result.getString("host")).isEqualTo("localhost");
	}
}
