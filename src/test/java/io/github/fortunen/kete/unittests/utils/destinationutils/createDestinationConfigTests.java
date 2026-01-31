package io.github.fortunen.kete.unittests.utils.destinationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.destinations.amqp091.Amqp091DestinationConfig;
import io.github.fortunen.kete.destinations.amqp1.Amqp1DestinationConfig;
import io.github.fortunen.kete.destinations.http.HttpDestinationConfig;
import io.github.fortunen.kete.destinations.kafka.KafkaDestinationConfig;
import io.github.fortunen.kete.destinations.mqtt3.Mqtt3DestinationConfig;
import io.github.fortunen.kete.destinations.mqtt5.Mqtt5DestinationConfig;
import io.github.fortunen.kete.utils.DestinationUtils;

public class createDestinationConfigTests {

	@Test
	public void shouldThrowWhenConfigurationIsNull() {

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestinationConfig(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("configuration is required");
	}

	@Test
	public void shouldThrowWhenKindIsMissing() {

		// arrange

		var configuration = new MapConfiguration(new HashMap<>());

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestinationConfig(configuration));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo(Constants.KIND + " is required");
	}

	@Test
	public void shouldThrowWhenKindIsEmpty() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, ""));

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestinationConfig(configuration));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo(Constants.KIND + " is required");
	}

	@Test
	public void shouldThrowWhenKindIsBlank() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "   "));

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestinationConfig(configuration));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo(Constants.KIND + " is required");
	}

	@Test
	public void shouldThrowWhenKindIsUnknown() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "unknown-destination"));

		// act

		var thrown = catchThrowable(() -> DestinationUtils.createDestinationConfig(configuration));

		// assert

		assertThat(thrown).isInstanceOf(RuntimeException.class);
		assertThat(thrown.getMessage()).isEqualTo("Failed to resolve DestinationConfig for kind 'unknown-destination'");
	}

	@Test
	public void shouldCreateHttpDestinationConfig() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "http"));

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(HttpDestinationConfig.class);
	}

	@Test
	public void shouldCreateKafkaDestinationConfig() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "kafka"));

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(KafkaDestinationConfig.class);
	}

	@Test
	public void shouldCreateAmqp1DestinationConfig() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "amqp-1"));

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Amqp1DestinationConfig.class);
	}

	@Test
	public void shouldCreateAmqp091DestinationConfig() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "amqp-0.9.1"));

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Amqp091DestinationConfig.class);
	}

	@Test
	public void shouldCreateMqtt3DestinationConfig() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "mqtt-3"));

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Mqtt3DestinationConfig.class);
	}

	@Test
	public void shouldCreateMqtt5DestinationConfig() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "mqtt-5"));

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Mqtt5DestinationConfig.class);
	}

	@Test
	public void shouldSetConfigurationOnCreatedConfig() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put(Constants.KIND, "http");
		map.put("url", "http://example.com/events");
		var configuration = new MapConfiguration(map);

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getConfiguration()).isSameAs(configuration);
	}

	@Test
	public void shouldTrimKindBeforeLookup() {

		// arrange

		var configuration = new MapConfiguration(Map.of(Constants.KIND, "  http  "));

		// act

		var result = DestinationUtils.createDestinationConfig(configuration);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(HttpDestinationConfig.class);
	}
}
