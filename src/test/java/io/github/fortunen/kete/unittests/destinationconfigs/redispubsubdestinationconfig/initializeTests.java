package io.github.fortunen.kete.unittests.destinationconfigs.redispubsubdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.redispubsub.RedisPubSubDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"channel", "test-channel"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	@Test
	public void shouldThrowWhenHostIsEmpty() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "",
			"channel", "test-channel"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	@Test
	public void shouldThrowWhenHostIsBlank() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "   ",
			"channel", "test-channel"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	// =========================================================================
	// Required Fields - Channel
	// =========================================================================

	@Test
	public void shouldThrowWhenChannelIsMissing() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("channel is required");
	}

	@Test
	public void shouldThrowWhenChannelIsEmpty() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("channel is required");
	}

	@Test
	public void shouldThrowWhenChannelIsBlank() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("channel is required");
	}

	// =========================================================================
	// Port
	// =========================================================================

	@Test
	public void shouldDefaultToPort6379WhenTlsDisabled() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(6379);
	}

	@Test
	public void shouldDefaultToPort6380WhenTlsEnabled() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(6380);
	}

	@Test
	public void shouldUseCustomPort() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"port", "16379"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(16379);
	}

	@Test
	public void shouldThrowWhenPortIsNegative() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"port", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	@Test
	public void shouldThrowWhenPortExceedsMax() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"port", "65536"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	// =========================================================================
	// Database
	// =========================================================================

	@Test
	public void shouldDefaultToDatabase0() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDatabase()).isEqualTo(0);
	}

	@Test
	public void shouldUseCustomDatabase() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"database", "5"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDatabase()).isEqualTo(5);
	}

	@Test
	public void shouldThrowWhenDatabaseIsNegative() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"database", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("database must be non-negative");
	}

	// =========================================================================
	// Username
	// =========================================================================

	@Test
	public void shouldDefaultToEmptyUsername() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEmpty();
	}

	@Test
	public void shouldUseCustomUsername() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"username", "redis-user"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("redis-user");
	}

	// =========================================================================
	// Password
	// =========================================================================

	@Test
	public void shouldDefaultToEmptyPassword() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEmpty();
	}

	@Test
	public void shouldUseCustomPassword() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"password", "secret123"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEqualTo("secret123");
	}

	// =========================================================================
	// Client Name
	// =========================================================================

	@Test
	public void shouldDefaultToKeteClientName() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientName()).isEqualTo("kete");
	}

	@Test
	public void shouldUseCustomClientName() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"client-name", "my-app"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientName()).isEqualTo("my-app");
	}

	// =========================================================================
	// Connection Timeout
	// =========================================================================

	@Test
	public void shouldDefaultToConnectionTimeout10() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(RedisPubSubDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomConnectionTimeout() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"connection-timeout-seconds", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldThrowWhenConnectionTimeoutIsNegative() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"connection-timeout-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("connection-timeout-seconds must be non-negative");
	}

	// =========================================================================
	// Command Timeout
	// =========================================================================

	@Test
	public void shouldDefaultToCommandTimeout60() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCommandTimeoutSeconds()).isEqualTo(RedisPubSubDestinationConfig.DEFAULT_COMMAND_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomCommandTimeout() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"command-timeout-seconds", "120"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCommandTimeoutSeconds()).isEqualTo(120);
	}

	@Test
	public void shouldThrowWhenCommandTimeoutIsNegative() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"command-timeout-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("command-timeout-seconds must be non-negative");
	}

	// =========================================================================
	// Redis URI
	// =========================================================================

	@Test
	public void shouldBuildRedisUri() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getRedisUri()).isNotNull();
		assertThat(config.getRedisUri().getHost()).isEqualTo("localhost");
		assertThat(config.getRedisUri().getPort()).isEqualTo(6379);
		assertThat(config.getRedisUri().getDatabase()).isEqualTo(0);
	}

	@Test
	public void shouldBuildRedisUriWithAuthentication() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"username", "redis-user",
			"password", "secret123"
		)));

		// act

		config.initialize();

		// assert

		var credentials = config.getRedisUri().getCredentialsProvider().resolveCredentials().block();

		assertThat(config.getRedisUri()).isNotNull();
		assertThat(credentials.getUsername()).isEqualTo("redis-user");
		assertThat(credentials.getPassword()).isEqualTo("secret123".toCharArray());
	}

	@Test
	public void shouldBuildRedisUriWithPasswordOnly() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"password", "secret123"
		)));

		// act

		config.initialize();

		// assert

		var credentials = config.getRedisUri().getCredentialsProvider().resolveCredentials().block();

		assertThat(config.getRedisUri()).isNotNull();
		assertThat(credentials.getPassword()).isEqualTo("secret123".toCharArray());
	}

	@Test
	public void shouldBuildRedisUriWithSslEnabled() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getRedisUri()).isNotNull();
		assertThat(config.getRedisUri().isSsl()).isTrue();
	}

	// =========================================================================
	// Client Options
	// =========================================================================

	@Test
	public void shouldBuildClientOptions() {

		// arrange

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-pubsub",
			"host", "localhost",
			"channel", "test-channel"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientOptions()).isNotNull();
		assertThat(config.getClientOptions().isAutoReconnect()).isTrue();
	}

	// =========================================================================
	// Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithAllOptions() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "redis-pubsub");
		configMap.put("host", "redis.example.com");
		configMap.put("channel", "events");
		configMap.put("port", "16379");
		configMap.put("database", "3");
		configMap.put("username", "user");
		configMap.put("password", "pass");
		configMap.put("client-name", "my-client");
		configMap.put("connection-timeout-seconds", "15");
		configMap.put("command-timeout-seconds", "90");

		var config = new RedisPubSubDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("redis.example.com");
		assertThat(config.getChannel()).isEqualTo("events");
		assertThat(config.getPort()).isEqualTo(16379);
		assertThat(config.getDatabase()).isEqualTo(3);
		assertThat(config.getUsername()).isEqualTo("user");
		assertThat(config.getPassword()).isEqualTo("pass");
		assertThat(config.getClientName()).isEqualTo("my-client");
		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(15);
		assertThat(config.getCommandTimeoutSeconds()).isEqualTo(90);
	}
}
