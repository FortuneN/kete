package io.github.fortunen.kete.unittests.destinationconfigs.natsjetstreamdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.destinations.natsjetstream.NatsJetStreamDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Servers
	// =========================================================================

	@Test
	public void shouldThrowWhenServersIsMissing() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"subject", "test-subject",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "",
			"subject", "test-subject",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "   ",
			"subject", "test-subject",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "   ",
			"stream", "test-stream",
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
	// Required Fields - Stream
	// =========================================================================

	@Test
	public void shouldThrowWhenStreamIsMissing() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required for JetStream destination");
	}

	@Test
	public void shouldThrowWhenStreamIsEmpty() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required for JetStream destination");
	}

	@Test
	public void shouldThrowWhenStreamIsBlank() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "   ",
			"authentication-method", "none"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required for JetStream destination");
	}

	// =========================================================================
	// Successful Initialization
	// =========================================================================

	@Test
	public void shouldInitializeWithMinimalConfiguration() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "test-stream",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getServers()).containsExactly("nats://localhost:4222");
		assertThat(config.getSubject()).isEqualTo("test-subject");
		assertThat(config.getStream()).isEqualTo("test-stream");
		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(NatsJetStreamDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
		assertThat(config.getPingIntervalSeconds()).isEqualTo(NatsJetStreamDestinationConfig.DEFAULT_PING_INTERVAL_SECONDS);
		assertThat(config.getConnectionName()).isEqualTo(Constants.ID);
		assertThat(config.getPublishTimeoutSeconds()).isEqualTo(NatsJetStreamDestinationConfig.DEFAULT_PUBLISH_TIMEOUT_SECONDS);
		assertThat(config.getPublishTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(config.getAuthMaterial()).isNotNull();
		assertThat(config.getNatsOptions()).isNotNull();
		assertThat(config.getNatsOptions().getMaxReconnect()).isEqualTo(-1);
		assertThat(config.getNatsOptions().getReconnectBufferSize()).isEqualTo(0);
	}

	@Test
	public void shouldParseMultipleServers() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://server1:4222, nats://server2:4222, nats://server3:4222",
			"subject", "test-subject",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "  nats://localhost:4222  ",
			"subject", "test-subject",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://server1:4222,,nats://server2:4222,  ,nats://server3:4222",
			"subject", "test-subject",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "test-stream",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(NatsJetStreamDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomConnectionTimeout() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "nats-jetstream");
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("stream", "test-stream");
		map.put("authentication-method", "none");
		map.put("connection-timeout-seconds", 30);
		var config = new NatsJetStreamDestinationConfig();
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
		map.put("kind", "nats-jetstream");
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("stream", "test-stream");
		map.put("authentication-method", "none");
		map.put("connection-timeout-seconds", -1);
		var config = new NatsJetStreamDestinationConfig();
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "test-stream",
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
		map.put("kind", "nats-jetstream");
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("stream", "test-stream");
		map.put("authentication-method", "none");
		map.put("ping-interval-seconds", 120);
		var config = new NatsJetStreamDestinationConfig();
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
		map.put("kind", "nats-jetstream");
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("stream", "test-stream");
		map.put("authentication-method", "none");
		map.put("ping-interval-seconds", -1);
		var config = new NatsJetStreamDestinationConfig();
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "test-stream",
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

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "test-stream",
			"authentication-method", "none",
			"connection-name", "my-jetstream-connection"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionName()).isEqualTo("my-jetstream-connection");
	}

	// =========================================================================
	// Publish Timeout Seconds
	// =========================================================================

	@Test
	public void shouldUseDefaultPublishTimeoutSeconds() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "test-stream",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPublishTimeoutSeconds()).isEqualTo(10);
		assertThat(config.getPublishTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	public void shouldUseCustomPublishTimeoutSeconds() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "nats-jetstream");
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("stream", "test-stream");
		map.put("authentication-method", "none");
		map.put("publish-timeout-seconds", 30);
		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPublishTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getPublishTimeout()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	public void shouldThrowWhenPublishTimeoutSecondsIsNegative() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "nats-jetstream");
		map.put("servers", "nats://localhost:4222");
		map.put("subject", "test-subject");
		map.put("stream", "test-stream");
		map.put("authentication-method", "none");
		map.put("publish-timeout-seconds", -1);
		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("publish-timeout-seconds must be non-negative");
	}

	// =========================================================================
	// NATS Options
	// =========================================================================

	@Test
	public void shouldBuildNatsOptions() {

		// arrange

		var config = new NatsJetStreamDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "nats-jetstream",
			"servers", "nats://localhost:4222",
			"subject", "test-subject",
			"stream", "test-stream",
			"authentication-method", "none"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getNatsOptions()).isNotNull();
		assertThat(config.getNatsOptions().getConnectionName()).isEqualTo(Constants.ID);
	}
}
