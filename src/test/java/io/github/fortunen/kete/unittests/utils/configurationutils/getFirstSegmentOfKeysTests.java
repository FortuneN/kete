package io.github.fortunen.kete.unittests.utils.configurationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.ConfigurationUtils;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class getFirstSegmentOfKeysTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> ConfigurationUtils.getFirstSegmentOfKeys(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldReturnEmptyArrayForEmptyConfiguration() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var result = ConfigurationUtils.getFirstSegmentOfKeys(config);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	void shouldReturnFirstSegmentOfDottedKeys() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("destination.host", "localhost");
		map.put("destination.port", "8080");
		map.put("filter.pattern", ".*");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getFirstSegmentOfKeys(config);

		// assert

		assertThat(result).containsExactlyInAnyOrder("destination", "filter");
	}

	@Test
	void shouldReturnFullKeyWhenNoDot() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("simplekey", "value");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getFirstSegmentOfKeys(config);

		// assert

		assertThat(result).containsExactly("simplekey");
	}

	@Test
	void shouldDeduplicateFirstSegments() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("destination.host", "localhost");
		map.put("destination.port", "8080");
		map.put("destination.protocol", "https");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getFirstSegmentOfKeys(config);

		// assert

		assertThat(result).containsExactly("destination");
	}

	@Test
	void shouldHandleMixedKeyFormats() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("simple", "value1");
		map.put("dotted.key", "value2");
		map.put("deeply.nested.key", "value3");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getFirstSegmentOfKeys(config);

		// assert

		assertThat(result).containsExactlyInAnyOrder("simple", "dotted", "deeply");
	}

	@Test
	void shouldTrimFirstSegment() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("  segment  .key", "value");
		var config = new MapConfiguration(map);

		// act

		var result = ConfigurationUtils.getFirstSegmentOfKeys(config);

		// assert

		assertThat(result).contains("segment");
	}
}
