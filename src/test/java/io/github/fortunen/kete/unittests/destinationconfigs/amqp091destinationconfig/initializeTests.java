package io.github.fortunen.kete.unittests.destinationconfigs.amqp091destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.amqp091.Amqp091DestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"exchange", "test-exchange"
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

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "",
			"exchange", "test-exchange"
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

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "   ",
			"exchange", "test-exchange"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	// =========================================================================
	// Required Fields - Exchange
	// =========================================================================

	@Test
	public void shouldThrowWhenExchangeIsMissing() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("exchange is required");
	}

	@Test
	public void shouldThrowWhenExchangeIsEmpty() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("exchange is required");
	}

	@Test
	public void shouldThrowWhenExchangeIsBlank() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("exchange is required");
	}

	// =========================================================================
	// Port Validation
	// =========================================================================

	@Test
	public void shouldUseDefaultPortWhenNotSpecified() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(Amqp091DestinationConfig.DEFAULT_PORT);
	}

	@Test
	public void shouldUseSpecifiedPort() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"port", 5673,
			"exchange", "test-exchange"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(5673);
	}

	@Test
	public void shouldThrowWhenPortIsZero() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"port", 0,
			"exchange", "test-exchange"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("port");
	}

	@Test
	public void shouldThrowWhenPortIsNegative() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"port", -1,
			"exchange", "test-exchange"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("port");
	}

	@Test
	public void shouldThrowWhenPortIsTooHigh() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"port", 65536,
			"exchange", "test-exchange"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("port");
	}

	// =========================================================================
	// Default Values
	// =========================================================================

	@Test
	public void shouldInitializeWithMinimalConfiguration() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("localhost");
		assertThat(config.getExchange()).isEqualTo("test-exchange");
		assertThat(config.getPort()).isEqualTo(Amqp091DestinationConfig.DEFAULT_PORT);
		assertThat(config.getVirtualHost()).isEqualTo(Amqp091DestinationConfig.DEFAULT_VIRTUAL_HOST);
		assertThat(config.getRoutingKey()).isEmpty();
		assertThat(config.getUsername()).isEmpty();
		assertThat(config.getPassword()).isEmpty();
		assertThat(config.getPriority()).isEqualTo(0);
		assertThat(config.isHasPriority()).isFalse();
		assertThat(config.getTimeToLiveSeconds()).isEqualTo(Amqp091DestinationConfig.DEFAULT_TIME_TO_LIVE_SECONDS);
		assertThat(config.isHasTimeToLiveSeconds()).isFalse();
		assertThat(config.getDeliveryMode()).isEqualTo(2); // persistent
		assertThat(config.getHandshakeTimeoutSeconds()).isEqualTo(Amqp091DestinationConfig.DEFAULT_HANDSHAKE_TIMEOUT_SECONDS);
		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(Amqp091DestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
		assertThat(config.getChannelRpcTimeoutSeconds()).isEqualTo(Amqp091DestinationConfig.DEFAULT_CHANNEL_RPC_TIMEOUT_SECONDS);
		assertThat(config.getRequestedHeartbeatSeconds()).isEqualTo(Amqp091DestinationConfig.DEFAULT_REQUESTED_HEARTBEAT_SECONDS);
		assertThat(config.getNetworkRecoveryIntervalSeconds()).isEqualTo(Amqp091DestinationConfig.DEFAULT_NETWORK_RECOVERY_INTERVAL_SECONDS);
		assertThat(config.isAutomaticRecoveryEnabled()).isTrue();
		assertThat(config.isTopologyRecoveryEnabled()).isTrue();
		assertThat(config.getTls().isEnabled()).isFalse();
		assertThat(config.getConnectionFactory()).isNotNull();
	}

	// =========================================================================
	// Optional String Fields
	// =========================================================================

	@Test
	public void shouldParseRoutingKey() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"routing-key", "my.routing.key"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getRoutingKey()).isEqualTo("my.routing.key");
	}

	@Test
	public void shouldParseUsername() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"username", "admin"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("admin");
	}

	@Test
	public void shouldParsePassword() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"password", "secret"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEqualTo("secret");
	}

	@Test
	public void shouldParseVirtualHost() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"virtual-host", "/my-vhost"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getVirtualHost()).isEqualTo("/my-vhost");
	}

	// =========================================================================
	// Priority
	// =========================================================================

	@Test
	public void shouldParsePriority() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"priority", 7
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPriority()).isEqualTo(7);
		assertThat(config.isHasPriority()).isTrue();
	}

	@Test
	public void shouldAcceptMinPriority() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"priority", 0
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPriority()).isEqualTo(0);
		assertThat(config.isHasPriority()).isTrue();
	}

	@Test
	public void shouldAcceptMaxPriority() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"priority", 9
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPriority()).isEqualTo(9);
		assertThat(config.isHasPriority()).isTrue();
	}

	@Test
	public void shouldThrowWhenPriorityIsTooLow() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"priority", -1
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("priority");
	}

	@Test
	public void shouldThrowWhenPriorityIsTooHigh() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"priority", 10
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("priority");
	}

	// =========================================================================
	// Delivery Mode
	// =========================================================================

	@Test
	public void shouldDefaultToPersistentDeliveryMode() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDeliveryModeString()).isEqualTo("persistent");
		assertThat(config.getDeliveryMode()).isEqualTo(2);
	}

	@Test
	public void shouldParsePersistentDeliveryMode() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"delivery-mode", "persistent"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDeliveryModeString()).isEqualTo("persistent");
		assertThat(config.getDeliveryMode()).isEqualTo(2);
	}

	@Test
	public void shouldParseNonPersistentDeliveryMode() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"delivery-mode", "non-persistent"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDeliveryModeString()).isEqualTo("non-persistent");
		assertThat(config.getDeliveryMode()).isEqualTo(1);
	}

	@Test
	public void shouldThrowWhenDeliveryModeIsInvalid() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"delivery-mode", "invalid"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("delivery-mode");
	}

	// =========================================================================
	// Time To Live
	// =========================================================================

	@Test
	public void shouldParseTimeToLive() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"time-to-live-seconds", 60L
		)));
		config.initialize();

		// assert

		assertThat(config.getTimeToLiveSeconds()).isEqualTo(60L);
		assertThat(config.isHasTimeToLiveSeconds()).isTrue();
	}

	@Test
	public void shouldThrowWhenTimeToLiveIsNegative() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"time-to-live-seconds", -1L
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("time-to-live");
	}

	// =========================================================================
	// Timeout Validations
	// =========================================================================

	@Test
	public void shouldParseTimeouts() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("handshake-timeout-seconds", 5);
		configMap.put("connection-timeout-seconds", 6);
		configMap.put("channel-rpc-timeout-seconds", 7);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getHandshakeTimeoutSeconds()).isEqualTo(5);
		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(6);
		assertThat(config.getChannelRpcTimeoutSeconds()).isEqualTo(7);
	}

	@Test
	public void shouldThrowWhenHandshakeTimeoutIsNegative() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("handshake-timeout-seconds", -1);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("handshake-timeout-seconds");
	}

	@Test
	public void shouldThrowWhenConnectionTimeoutIsNegative() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("connection-timeout-seconds", -1);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("connection-timeout-seconds");
	}

	@Test
	public void shouldThrowWhenChannelRpcTimeoutIsNegative() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("channel-rpc-timeout-seconds", -1);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("channel-rpc-timeout-seconds");
	}

	// =========================================================================
	// Recovery Settings
	// =========================================================================

	@Test
	public void shouldParseRecoverySettings() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("automatic-recovery-enabled", false);
		configMap.put("topology-recovery-enabled", false);
		configMap.put("requested-heartbeat-seconds", 60);
		configMap.put("network-recovery-interval-seconds", 10);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.isAutomaticRecoveryEnabled()).isFalse();
		assertThat(config.isTopologyRecoveryEnabled()).isFalse();
		assertThat(config.getRequestedHeartbeatSeconds()).isEqualTo(60);
		assertThat(config.getNetworkRecoveryIntervalSeconds()).isEqualTo(10);
	}

	@Test
	public void shouldThrowWhenRequestedHeartbeatIsNegative() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("requested-heartbeat-seconds", -1);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("requested-heartbeat-seconds");
	}

	@Test
	public void shouldThrowWhenNetworkRecoveryIntervalIsZero() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("network-recovery-interval-seconds", 0);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("network-recovery-interval-seconds");
	}

	@Test
	public void shouldThrowWhenNetworkRecoveryIntervalIsNegative() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("network-recovery-interval-seconds", -1);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("network-recovery-interval-seconds");
	}

	// =========================================================================
	// Pool Size (inherited from DestinationConfig)
	// =========================================================================

	@Test
	public void shouldParsePoolSizes() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("pool.min-idle", 10);
		configMap.put("pool.max-total", 50);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getPoolMinIdle()).isEqualTo(10);
		assertThat(config.getPoolMaxTotal()).isEqualTo(50);
	}

	@Test
	public void shouldThrowWhenMinPoolSizeIsZero() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("pool.min-idle", 0);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("min-idle");
	}

	@Test
	public void shouldThrowWhenMaxPoolSizeLessThanMinPoolSize() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("pool.min-idle", 20);
		configMap.put("pool.max-total", 10);
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("max-total");
	}

	// =========================================================================
	// Connection Factory
	// =========================================================================

	@Test
	public void shouldCreateConnectionFactoryWithCorrectSettings() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "rabbitmq.example.com");
		configMap.put("port", 5673);
		configMap.put("exchange", "test-exchange");
		configMap.put("username", "admin");
		configMap.put("password", "secret");
		configMap.put("virtual-host", "/my-vhost");
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		var factory = config.getConnectionFactory();
		assertThat(factory).isNotNull();
		assertThat(factory.getHost()).isEqualTo("rabbitmq.example.com");
		assertThat(factory.getPort()).isEqualTo(5673);
		assertThat(factory.getUsername()).isEqualTo("admin");
		assertThat(factory.getPassword()).isEqualTo("secret");
		assertThat(factory.getVirtualHost()).isEqualTo("/my-vhost");
	}

	// =========================================================================
	// tls Configuration (parsed but not connected)
	// =========================================================================

	@Test
	public void shouldParseTlsEnabled() {

		// arrange

		var config = new Amqp091DestinationConfig();
		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "amqp-0.9.1");
		configMap.put("host", "localhost");
		configMap.put("exchange", "test-exchange");
		configMap.put("tls.enabled", "true");
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
	}

	// =========================================================================
	// Publisher Confirms
	// =========================================================================

	@Test
	public void shouldDefaultPublisherConfirmsToEnabled() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isPublisherConfirms()).isTrue();
		assertThat(config.getConfirmTimeoutSeconds()).isEqualTo(Amqp091DestinationConfig.DEFAULT_CONFIRM_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldReadCustomPublisherConfirms() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"publisher-confirms", false
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isPublisherConfirms()).isFalse();
	}

	@Test
	public void shouldReadCustomConfirmTimeoutSeconds() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"confirm-timeout-seconds", 10
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConfirmTimeoutSeconds()).isEqualTo(10);
	}

	@Test
	public void shouldThrowWhenConfirmTimeoutSecondsIsZero() {

		// arrange

		var config = new Amqp091DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "amqp-0.9.1",
			"host", "localhost",
			"exchange", "test-exchange",
			"confirm-timeout-seconds", 0
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("confirm-timeout-seconds must be positive");
	}
}
