package io.github.fortunen.kete.unittests.utils.configurationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.ConfigurationUtils;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class getKeysTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> ConfigurationUtils.getKeys(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldReturnEmptyArrayForEmptyConfiguration() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var result = ConfigurationUtils.getKeys(config);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	void shouldReturnAllKeys() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getKeys(config);

		// assert

		assertThat(result).containsExactlyInAnyOrder("key1", "key2", "key3");
	}

	@Test
	void shouldTrimKeys() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("  key1  ", "value1");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getKeys(config);

		// assert

		assertThat(result).contains("key1");
	}
}
