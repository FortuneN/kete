package io.github.fortunen.kete.unittests.destinationconfigs.natsdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.destinations.nats.NatsDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Servers
	// =========================================================================

	@Test
	public void shouldThrowWhenServersIsMissing() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("servers is required");
	}

	@Test
	public void shouldThrowWhenServersIsEmpty() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("servers is required");
	}

	@Test
	public void shouldThrowWhenServersIsBlank() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "   ",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("servers is required");
	}

	// =========================================================================
	// Required Fields - Subject
	// =========================================================================

	@Test
	public void shouldThrowWhenSubjectIsMissing() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("subject is required");
	}

	@Test
	public void shouldThrowWhenSubjectIsEmpty() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("subject is required");
	}

	@Test
	public void shouldThrowWhenSubjectIsBlank() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "   ",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("subject is required");
	}

	// =========================================================================
	// Successful Initialization
	// =========================================================================

	@Test
	public void shouldInitializeWithMinimalConfiguration() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getServers()).containsExactly("nats://localhost:4222");
		assertThat(config.getSubject()).isEqualTo("test-subject");
		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(NatsDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
		assertThat(config.getPingIntervalSeconds()).isEqualTo(NatsDestinationConfig.DEFAULT_PING_INTERVAL_SECONDS);
		assertThat(config.getConnectionName()).isEqualTo(Constants.ID);
		assertThat(config.getAuthMaterial()).isNotNull();
		assertThat(config.getNatsOptions()).isNotNull();
	}

	@Test
	public void shouldParseMultipleServers() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://server1:4222, nats://server2:4222, nats://server3:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getServers()).containsExactly("nats://server1:4222", "nats://server2:4222", "nats://server3:4222");
	}

	@Test
	public void shouldTrimServers() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "  nats://localhost:4222  ",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getServers()).containsExactly("nats://localhost:4222");
	}

	@Test
	public void shouldFilterEmptyServers() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://server1:4222,,nats://server2:4222,  ,nats://server3:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getServers()).containsExactly("nats://server1:4222", "nats://server2:4222", "nats://server3:4222");
	}

	// =========================================================================
	// Connection Timeout
	// =========================================================================

	@Test
	public void shouldUseDefaultConnectionTimeout() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(NatsDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomConnectionTimeout() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("authentication-method", "none");
		map.put("connection-timeout-seconds", 30);
		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldThrowWhenConnectionTimeoutIsNegative() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("authentication-method", "none");
		map.put("connection-timeout-seconds", -1);
		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("connection-timeout-seconds must be non-negative");
	}

	// =========================================================================
	// Ping Interval Seconds
	// =========================================================================

	@Test
	public void shouldUseDefaultPingIntervalSeconds() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPingIntervalSeconds()).isEqualTo(60);
	}

	@Test
	public void shouldUseCustomPingIntervalSeconds() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("authentication-method", "none");
		map.put("ping-interval-seconds", 120);
		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPingIntervalSeconds()).isEqualTo(120);
	}

	@Test
	public void shouldThrowWhenPingIntervalSecondsIsNegative() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("authentication-method", "none");
		map.put("ping-interval-seconds", -1);
		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("ping-interval-seconds must be non-negative");
	}

	// =========================================================================
	// Connection Name
	// =========================================================================

	@Test
	public void shouldUseDefaultConnectionName() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionName()).isEqualTo(Constants.ID);
	}

	@Test
	public void shouldUseCustomConnectionName() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"authentication-method", "none",
			"connection-name", "my-custom-connection"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionName()).isEqualTo("my-custom-connection");
	}

	// =========================================================================
	// Message Headers Enabled
	// =========================================================================

	// =========================================================================
	// NATS Options
	// =========================================================================

	@Test
	public void shouldBuildNatsOptions() {

		// arrange

		var config = new NatsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getNatsOptions()).isNotNull();
		assertThat(config.getNatsOptions().getConnectionName()).isEqualTo(Constants.ID);
	}
}
