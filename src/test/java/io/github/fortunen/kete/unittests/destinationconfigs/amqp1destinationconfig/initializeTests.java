package io.github.fortunen.kete.unittests.destinationconfigs.amqp1destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.amqp1.Amqp1DestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"destination-name", "test-queue"
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

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "",
			"destination-name", "test-queue"
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

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "   ",
			"destination-name", "test-queue"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	// =========================================================================
	// Required Fields - Destination Name
	// =========================================================================

	@Test
	public void shouldThrowWhenDestinationNameIsMissing() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination-name is required");
	}

	@Test
	public void shouldThrowWhenDestinationNameIsEmpty() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination-name is required");
	}

	@Test
	public void shouldThrowWhenDestinationNameIsBlank() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination-name is required");
	}

	// =========================================================================
	// Port - Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTcpPortWhenTlsDisabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(5672);
	}

	@Test
	public void shouldUseDefaultTlsPortWhenTlsEnabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(5671);
	}

	@Test
	public void shouldUseDefaultWebSocketPortWhenWebSocketAndTlsDisabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"transport-type", "amqp-web-sockets"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(443);
	}

	@Test
	public void shouldUseDefaultWebSocketPortWhenWebSocketAndTlsEnabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"transport-type", "amqp-web-sockets",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(443);
	}

	@Test
	public void shouldUseCustomPort() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"port", "15672"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(15672);
	}

	// =========================================================================
	// Port - Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenPortIsZero() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"port", "0"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	@Test
	public void shouldThrowWhenPortIsNegative() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
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
	public void shouldThrowWhenPortIsTooLarge() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"port", "65536"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	@Test
	public void shouldAcceptMinimumPort() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"port", "1"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(1);
	}

	@Test
	public void shouldAcceptMaximumPort() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"port", "65535"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(65535);
	}

	// =========================================================================
	// Transport Type
	// =========================================================================

	@Test
	public void shouldDefaultToAmqpTransportType() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTransportType()).isEqualTo("amqp");
	}

	@Test
	public void shouldAcceptAmqpWebSocketsTransportType() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"transport-type", "amqp-web-sockets"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTransportType()).isEqualTo("amqp-web-sockets");
	}

	@Test
	public void shouldThrowWhenTransportTypeIsInvalid() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"transport-type", "invalid"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("transport-type must be 'amqp' or 'amqp-web-sockets'");
	}

	@Test
	public void shouldNormalizeTransportTypeToLowerCase() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"transport-type", "AMQP-WEB-SOCKETS"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTransportType()).isEqualTo("amqp-web-sockets");
	}

	// =========================================================================
	// Destination Type
	// =========================================================================

	@Test
	public void shouldDefaultToQueueDestinationType() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDestinationType()).isEqualTo("queue");
		assertThat(config.isDestinationIsQueue()).isTrue();
	}

	@Test
	public void shouldAcceptTopicDestinationType() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-topic",
			"destination-type", "topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDestinationType()).isEqualTo("topic");
		assertThat(config.isDestinationIsQueue()).isFalse();
	}

	@Test
	public void shouldThrowWhenDestinationTypeIsInvalid() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-dest",
			"destination-type", "invalid"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("destination-type must be 'queue' or 'topic'");
	}

	@Test
	public void shouldNormalizeDestinationTypeToLowerCase() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-topic",
			"destination-type", "TOPIC"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDestinationType()).isEqualTo("topic");
	}

	// =========================================================================
	// URL Construction
	// =========================================================================

	@Test
	public void shouldConstructAmqpUrlWhenTlsDisabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("amqp://localhost:5672?amqp.idleTimeout=60000");
		assertThat(config.getScheme()).isEqualTo("amqp");
	}

	@Test
	public void shouldConstructAmqpsUrlWhenTlsEnabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("amqps://localhost:5671?amqp.idleTimeout=60000");
		assertThat(config.getScheme()).isEqualTo("amqps");
	}

	@Test
	public void shouldConstructAmqpwsUrlWhenWebSocketsAndTlsDisabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"transport-type", "amqp-web-sockets"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("amqpws://localhost:443?amqp.idleTimeout=60000");
		assertThat(config.getScheme()).isEqualTo("amqpws");
	}

	@Test
	public void shouldConstructAmqpwssUrlWhenWebSocketsAndTlsEnabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"transport-type", "amqp-web-sockets",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("amqpwss://localhost:443?amqp.idleTimeout=60000");
		assertThat(config.getScheme()).isEqualTo("amqpwss");
	}

	@Test
	public void shouldConstructUrlWithCustomPort() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "my-broker.example.com",
			"destination-name", "test-queue",
			"port", "15672"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("amqp://my-broker.example.com:15672?amqp.idleTimeout=60000");
	}

	// =========================================================================
	// Azure Service Bus Auto-TLS
	// =========================================================================

	@Test
	public void shouldAutoEnableTlsForAzureServiceBus() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("host", "my-namespace.servicebus.windows.net");
		configMap.put("destination-name", "test-queue");

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
		assertThat(config.getScheme()).isEqualTo("amqps");
		assertThat(config.getUrl()).isEqualTo("amqps://my-namespace.servicebus.windows.net:5671?amqp.idleTimeout=60000");
	}

	@Test
	public void shouldAutoEnableTlsForAzureServiceBusWithWebSockets() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("host", "my-namespace.servicebus.windows.net");
		configMap.put("destination-name", "test-queue");
		configMap.put("transport-type", "amqp-web-sockets");

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
		assertThat(config.getScheme()).isEqualTo("amqpwss");
		assertThat(config.getUrl()).isEqualTo("amqpwss://my-namespace.servicebus.windows.net:443?amqp.idleTimeout=60000");
	}

	// =========================================================================
	// Delivery Mode
	// =========================================================================

	@Test
	public void shouldDefaultToPersistentDeliveryMode() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDeliveryModeString()).isEqualTo("persistent");
		assertThat(config.getDeliveryMode()).isEqualTo(2); // DeliveryMode.PERSISTENT
	}

	@Test
	public void shouldAcceptNonPersistentDeliveryMode() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"delivery-mode", "non-persistent"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDeliveryModeString()).isEqualTo("non-persistent");
		assertThat(config.getDeliveryMode()).isEqualTo(1); // DeliveryMode.NON_PERSISTENT
	}

	@Test
	public void shouldThrowWhenDeliveryModeIsInvalid() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"delivery-mode", "invalid"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("delivery-mode must be 'persistent' or 'non-persistent'");
	}

	@Test
	public void shouldNormalizeDeliveryModeToLowerCase() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"delivery-mode", "NON-PERSISTENT"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDeliveryModeString()).isEqualTo("non-persistent");
	}

	// =========================================================================
	// Priority
	// =========================================================================

	@Test
	public void shouldDefaultToMidRangePriority() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPriority()).isEqualTo(4);
	}

	@Test
	public void shouldAcceptMinimumPriority() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"priority", "0"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPriority()).isEqualTo(0);
	}

	@Test
	public void shouldAcceptMaximumPriority() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"priority", "9"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPriority()).isEqualTo(9);
	}

	@Test
	public void shouldThrowWhenPriorityIsBelowMinimum() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"priority", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("priority must be between 0 and 9");
	}

	@Test
	public void shouldThrowWhenPriorityIsAboveMaximum() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"priority", "10"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("priority must be between 0 and 9");
	}

	// =========================================================================
	// Time To Live
	// =========================================================================

	@Test
	public void shouldDefaultToZeroTimeToLive() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeToLiveSeconds()).isEqualTo(0L);
	}

	@Test
	public void shouldAcceptPositiveTimeToLive() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"time-to-live-seconds", "60"
		)));
		config.initialize();

		// assert

		assertThat(config.getTimeToLiveSeconds()).isEqualTo(60L);
	}

	@Test
	public void shouldThrowWhenTimeToLiveIsNegative() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"time-to-live-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("time-to-live-seconds must be non-negative");
	}

	// =========================================================================
	// Username & Password
	// =========================================================================

	@Test
	public void shouldDefaultToEmptyUsername() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEmpty();
	}

	@Test
	public void shouldDefaultToEmptyPassword() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEmpty();
	}

	@Test
	public void shouldAcceptUsernameAndPassword() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"username", "myuser",
			"password", "mypass"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("myuser");
		assertThat(config.getPassword()).isEqualTo("mypass");
	}

	@Test
	public void shouldTrimUsername() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"username", "  myuser  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("myuser");
	}

	@Test
	public void shouldTrimPassword() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"password", "  mypass  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEqualTo("mypass");
	}

	// =========================================================================
	// Connection Factory
	// =========================================================================

	@Test
	public void shouldCreateConnectionFactory() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionFactory()).isNotNull();
	}

	@Test
	public void shouldSetUsernameOnConnectionFactory() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"username", "testuser"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionFactory()).isNotNull();
		assertThat(config.getConnectionFactory().getUsername()).isEqualTo("testuser");
	}

	// =========================================================================
	// TLS
	// =========================================================================

	@Test
	public void shouldDefaultToTlsDisabled() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isFalse();
	}

	@Test
	public void shouldEnableTls() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
	}

	// =========================================================================
	// Idle Timeout (Keep-Alive)
	// =========================================================================

	@Test
	public void shouldUseDefaultIdleTimeout() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getIdleTimeoutSeconds()).isEqualTo(Amqp1DestinationConfig.DEFAULT_IDLE_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomIdleTimeout() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"idle-timeout-seconds", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getIdleTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldDisableIdleTimeoutWhenSetToZero() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"idle-timeout-seconds", "0"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getIdleTimeoutSeconds()).isEqualTo(0);
	}

	@Test
	public void shouldThrowWhenIdleTimeoutIsNegative() {

		// arrange

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"destination-name", "test-queue",
			"idle-timeout-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("idle-timeout-seconds must be non-negative");
	}

	// =========================================================================
	// Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithFullConfiguration() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("host", "my-broker.example.com");
		configMap.put("port", "5673");
		configMap.put("destination-name", "my-queue");
		configMap.put("destination-type", "queue");
		configMap.put("transport-type", "amqp");
		configMap.put("username", "admin");
		configMap.put("password", "secret");
		configMap.put("delivery-mode", "non-persistent");
		configMap.put("priority", "7");
		configMap.put("time-to-live-seconds", "30");

		var config = new Amqp1DestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("my-broker.example.com");
		assertThat(config.getPort()).isEqualTo(5673);
		assertThat(config.getQueueOrTopicName()).isEqualTo("my-queue");
		assertThat(config.getDestinationType()).isEqualTo("queue");
		assertThat(config.isDestinationIsQueue()).isTrue();
		assertThat(config.getTransportType()).isEqualTo("amqp");
		assertThat(config.getUsername()).isEqualTo("admin");
		assertThat(config.getPassword()).isEqualTo("secret");
		assertThat(config.getDeliveryModeString()).isEqualTo("non-persistent");
		assertThat(config.getDeliveryMode()).isEqualTo(1);
		assertThat(config.getPriority()).isEqualTo(7);
		assertThat(config.getTimeToLiveSeconds()).isEqualTo(30L);
		assertThat(config.getUrl()).isEqualTo("amqp://my-broker.example.com:5673?amqp.idleTimeout=60000");
		assertThat(config.getScheme()).isEqualTo("amqp");
		assertThat(config.getConnectionFactory()).isNotNull();
	}
}
