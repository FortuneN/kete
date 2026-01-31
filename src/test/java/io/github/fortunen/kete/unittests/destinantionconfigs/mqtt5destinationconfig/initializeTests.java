package io.github.fortunen.kete.unittests.destinantionconfigs.mqtt5destinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.mqtt5.Mqtt5DestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"topic", "test-topic"
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

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "",
			"topic", "test-topic"
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

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "   ",
			"topic", "test-topic"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	// =========================================================================
	// Required Fields - Topic
	// =========================================================================

	@Test
	public void shouldThrowWhenTopicIsMissing() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	@Test
	public void shouldThrowWhenTopicIsEmpty() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", ""
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	@Test
	public void shouldThrowWhenTopicIsBlank() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("topic is required");
	}

	// =========================================================================
	// Transport Type
	// =========================================================================

	@Test
	public void shouldDefaultToTcpTransportType() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTransportType()).isEqualTo("tcp");
	}

	@Test
	public void shouldAcceptWebsocketTransportType() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"transport-type", "websocket"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTransportType()).isEqualTo("websocket");
	}

	@Test
	public void shouldThrowWhenTransportTypeIsInvalid() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"transport-type", "invalid"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("transport-type must be 'tcp' or 'websocket'");
	}

	@Test
	public void shouldNormalizeTransportTypeToLowerCase() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"transport-type", "WEBSOCKET"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTransportType()).isEqualTo("websocket");
	}

	// =========================================================================
	// Port - Defaults Based on Transport Type and TLS
	// =========================================================================

	@Test
	public void shouldUseDefaultTcpPortWhenTlsDisabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(1883);
	}

	@Test
	public void shouldUseDefaultTlsPortWhenTlsEnabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(8883);
	}

	@Test
	public void shouldUseDefaultWsPortWhenWebSocketAndTlsDisabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"transport-type", "websocket"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(8000);
	}

	@Test
	public void shouldUseDefaultWssPortWhenWebSocketAndTlsEnabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"transport-type", "websocket",
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

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"port", "11883"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(11883);
	}

	// =========================================================================
	// Port - Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenPortIsZero() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
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

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
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

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
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

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
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

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"port", "65535"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(65535);
	}

	// =========================================================================
	// URL Construction
	// =========================================================================

	@Test
	public void shouldConstructTcpUrlWhenTlsDisabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("tcp://localhost:1883");
		assertThat(config.getScheme()).isEqualTo("tcp://");
	}

	@Test
	public void shouldConstructSslUrlWhenTlsEnabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("ssl://localhost:8883");
		assertThat(config.getScheme()).isEqualTo("ssl://");
	}

	@Test
	public void shouldConstructWsUrlWhenWebSocketAndTlsDisabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"transport-type", "websocket"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("ws://localhost:8000");
		assertThat(config.getScheme()).isEqualTo("ws://");
	}

	@Test
	public void shouldConstructWssUrlWhenWebSocketAndTlsEnabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"transport-type", "websocket",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("wss://localhost:443");
		assertThat(config.getScheme()).isEqualTo("wss://");
	}

	// =========================================================================
	// QoS
	// =========================================================================

	@Test
	public void shouldDefaultToQosOne() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getQos()).isEqualTo(1);
	}

	@Test
	public void shouldAcceptQosZero() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"qos", "0"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getQos()).isEqualTo(0);
	}

	@Test
	public void shouldAcceptQosTwo() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"qos", "2"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getQos()).isEqualTo(2);
	}

	@Test
	public void shouldThrowWhenQosIsBelowMinimum() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"qos", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("qos must be 0, 1 or 2");
	}

	@Test
	public void shouldThrowWhenQosIsAboveMaximum() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"qos", "3"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("qos must be 0, 1 or 2");
	}

	// =========================================================================
	// Retained
	// =========================================================================

	@Test
	public void shouldDefaultToRetainedFalse() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isRetained()).isFalse();
	}

	@Test
	public void shouldAcceptRetainedTrue() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"retained", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isRetained()).isTrue();
	}

	// =========================================================================
	// Message Headers - Supported in MQTT 5
	// =========================================================================

	@Test
	public void shouldDefaultToMessageHeadersEnabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isMessageHeadersEnabled()).isTrue();
	}

	@Test
	public void shouldDisableMessageHeaders() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"message-headers-enabled", "false"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isMessageHeadersEnabled()).isFalse();
	}

	// =========================================================================
	// Client ID Prefix
	// =========================================================================

	@Test
	public void shouldGenerateClientIdPrefixWhenNotProvided() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientIdPrefix()).isNotEmpty();
	}

	@Test
	public void shouldUseProvidedClientIdPrefix() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"client-id-prefix", "kete-"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getClientIdPrefix()).isEqualTo("kete-");
	}

	// =========================================================================
	// Clean Session (Clean Start in MQTT 5)
	// =========================================================================

	@Test
	public void shouldDefaultToCleanSessionTrue() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isCleanSession()).isTrue();
		assertThat(config.getConnectOptions().isCleanStart()).isTrue();
	}

	@Test
	public void shouldAcceptCleanSessionFalse() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"clean-session", "false"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isCleanSession()).isFalse();
	}

	// =========================================================================
	// Connection Timeout
	// =========================================================================

	@Test
	public void shouldDefaultToTenSecondsConnectionTimeout() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeout()).isEqualTo(10);
		assertThat(config.getConnectOptions().getConnectionTimeout()).isEqualTo(10);
	}

	@Test
	public void shouldAcceptCustomConnectionTimeout() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"connection-timeout", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeout()).isEqualTo(30);
	}

	// =========================================================================
	// Keep Alive Interval
	// =========================================================================

	@Test
	public void shouldDefaultToSixtySecondsKeepAlive() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getKeepAliveInterval()).isEqualTo(60);
		assertThat(config.getConnectOptions().getKeepAliveInterval()).isEqualTo(60);
	}

	@Test
	public void shouldAcceptCustomKeepAliveInterval() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"keep-alive-interval", "120"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getKeepAliveInterval()).isEqualTo(120);
	}

	// =========================================================================
	// Username and Password
	// =========================================================================

	@Test
	public void shouldDefaultToEmptyUsername() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEmpty();
	}

	@Test
	public void shouldDefaultToEmptyPassword() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEmpty();
	}

	@Test
	public void shouldAcceptUsernameAndPassword() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"username", "mqtt5user",
			"password", "mqtt5pass"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("mqtt5user");
		assertThat(config.getPassword()).isEqualTo("mqtt5pass");
	}

	@Test
	public void shouldSetUsernameOnConnectOptions() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"username", "mqtt5user"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectOptions().getUserName()).isEqualTo("mqtt5user");
	}

	@Test
	public void shouldSetPasswordOnConnectOptionsAsBytes() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"password", "mqtt5pass"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectOptions().getPassword()).isEqualTo("mqtt5pass".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void shouldTrimUsername() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"username", "  mqtt5user  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUsername()).isEqualTo("mqtt5user");
	}

	@Test
	public void shouldTrimPassword() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"password", "  mqtt5pass  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPassword()).isEqualTo("mqtt5pass");
	}

	// =========================================================================
	// Connect Options
	// =========================================================================

	@Test
	public void shouldCreateConnectOptions() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectOptions()).isNotNull();
	}

	@Test
	public void shouldEnableAutomaticReconnect() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectOptions().isAutomaticReconnect()).isTrue();
	}

	// =========================================================================
	// TLS
	// =========================================================================

	@Test
	public void shouldDefaultToTlsDisabled() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isFalse();
	}

	@Test
	public void shouldEnableTls() {

		// arrange

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "localhost",
			"topic", "test-topic",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
	}

	// =========================================================================
	// Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithFullConfiguration() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("host", "mqtt5.example.com");
		configMap.put("port", "8885");
		configMap.put("topic", "keycloak/events/v5");
		configMap.put("transport-type", "tcp");
		configMap.put("qos", "2");
		configMap.put("retained", "true");
		configMap.put("username", "admin");
		configMap.put("password", "secret");
		configMap.put("client-id-prefix", "kete-v5-");
		configMap.put("clean-session", "false");
		configMap.put("connection-timeout", "15");
		configMap.put("keep-alive-interval", "45");
		configMap.put("message-headers-enabled", "true");
		configMap.put("tls.enabled", "true");

		var config = new Mqtt5DestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("mqtt5.example.com");
		assertThat(config.getPort()).isEqualTo(8885);
		assertThat(config.getTopic()).isEqualTo("keycloak/events/v5");
		assertThat(config.getTransportType()).isEqualTo("tcp");
		assertThat(config.getQos()).isEqualTo(2);
		assertThat(config.isRetained()).isTrue();
		assertThat(config.getUsername()).isEqualTo("admin");
		assertThat(config.getPassword()).isEqualTo("secret");
		assertThat(config.getClientIdPrefix()).isEqualTo("kete-v5-");
		assertThat(config.isCleanSession()).isFalse();
		assertThat(config.getConnectionTimeout()).isEqualTo(15);
		assertThat(config.getKeepAliveInterval()).isEqualTo(45);
		assertThat(config.isMessageHeadersEnabled()).isTrue();
		assertThat(config.getTls().isEnabled()).isTrue();
		assertThat(config.getUrl()).isEqualTo("ssl://mqtt5.example.com:8885");
		assertThat(config.getScheme()).isEqualTo("ssl://");
		assertThat(config.getConnectOptions()).isNotNull();
	}
}
