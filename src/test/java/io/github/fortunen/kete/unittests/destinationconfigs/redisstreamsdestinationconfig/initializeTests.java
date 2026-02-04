package io.github.fortunen.kete.unittests.destinationconfigs.redisstreamsdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.redisstreams.RedisStreamsDestinationConfig;

public class initializeTests {

	private Map<String, Object> createFullConfig() {
		var map = new java.util.HashMap<String, Object>();
		map.put("kind", "redis-streams");
		map.put("host", "redis.example.com");
		map.put("stream", "events");
		map.put("port", "16379");
		map.put("database", "3");
		map.put("username", "user");
		map.put("password", "pass");
		map.put("client-name", "my-client");
		map.put("connection-timeout-seconds", "15");
		map.put("command-timeout-seconds", "90");
		map.put("max-len", "5000");
		map.put("approximate-trimming", "false");
		return map;
	}

	// =========================================================================
	// Required Fields - Host
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"stream", "test-stream"
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "",
			"stream", "test-stream"
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "   ",
			"stream", "test-stream"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	// =========================================================================
	// Required Fields - Stream
	// =========================================================================

	@Test
	public void shouldThrowWhenStreamIsMissing() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required");
	}

	@Test
	public void shouldThrowWhenStreamIsEmpty() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required");
	}

	@Test
	public void shouldThrowWhenStreamIsBlank() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("stream is required");
	}

	// =========================================================================
	// Port
	// =========================================================================

	@Test
	public void shouldDefaultToPort6379WhenTlsDisabled() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(6379);
	}

	@Test
	public void shouldDefaultToPort6380WhenTlsEnabled() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDatabase()).isEqualTo(0);
	}

	@Test
	public void shouldUseCustomDatabase() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEmpty();
	}

	@Test
	public void shouldUseCustomUsername() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEmpty();
	}

	@Test
	public void shouldUseCustomPassword() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientName()).isEqualTo("kete");
	}

	@Test
	public void shouldUseCustomClientName() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(RedisStreamsDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomConnectionTimeout() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCommandTimeoutSeconds()).isEqualTo(RedisStreamsDestinationConfig.DEFAULT_COMMAND_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomCommandTimeout() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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
	// Max Len
	// =========================================================================

	@Test
	public void shouldDefaultToMaxLen0() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getMaxLen()).isEqualTo(0);
	}

	@Test
	public void shouldUseCustomMaxLen() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
			"max-len", "1000"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getMaxLen()).isEqualTo(1000);
	}

	@Test
	public void shouldThrowWhenMaxLenIsNegative() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
			"max-len", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("max-len must be non-negative");
	}

	// =========================================================================
	// Approximate Trimming
	// =========================================================================

	@Test
	public void shouldDefaultToApproximateTrimmingTrue() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isApproximateTrimming()).isTrue();
	}

	@Test
	public void shouldUseCustomApproximateTrimming() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
			"approximate-trimming", "false"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isApproximateTrimming()).isFalse();
	}

	// =========================================================================
	// Redis URI
	// =========================================================================

	@Test
	public void shouldBuildRedisUri() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
			"username", "redis-user",
			"password", "secret123"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getRedisUri()).isNotNull();
		assertThat(config.getRedisUri().getUsername()).isEqualTo("redis-user");
		assertThat(config.getRedisUri().getPassword()).isEqualTo("secret123".toCharArray());
	}

	@Test
	public void shouldBuildRedisUriWithPasswordOnly() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
			"password", "secret123"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getRedisUri()).isNotNull();
		assertThat(config.getRedisUri().getPassword()).isEqualTo("secret123".toCharArray());
	}

	@Test
	public void shouldBuildRedisUriWithSslEnabled() {

		// arrange

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream",
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "redis-streams",
			"host", "localhost",
			"stream", "test-stream"
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

		var config = new RedisStreamsDestinationConfig();
		config.setConfiguration(new MapConfiguration(createFullConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("redis.example.com");
		assertThat(config.getStream()).isEqualTo("events");
		assertThat(config.getPort()).isEqualTo(16379);
		assertThat(config.getDatabase()).isEqualTo(3);
		assertThat(config.getUsername()).isEqualTo("user");
		assertThat(config.getPassword()).isEqualTo("pass");
		assertThat(config.getClientName()).isEqualTo("my-client");
		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(15);
		assertThat(config.getCommandTimeoutSeconds()).isEqualTo(90);
		assertThat(config.getMaxLen()).isEqualTo(5000);
		assertThat(config.isApproximateTrimming()).isFalse();
	}
}
