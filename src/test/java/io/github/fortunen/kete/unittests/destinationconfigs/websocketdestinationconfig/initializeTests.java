package io.github.fortunen.kete.unittests.destinationconfigs.websocketdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.websocket.WebSocketDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host (when URL not provided)
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of()));

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

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", ""
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

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "   "
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("host is required");
	}

	// =========================================================================
	// URL Configuration - Full URL
	// =========================================================================

	@Test
	public void shouldParseFullWsUrl() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"url", "ws://example.com:8080/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("ws://example.com:8080/events");
		assertThat(config.getScheme()).isEqualTo("ws");
		assertThat(config.getTls().isEnabled()).isFalse();
	}

	@Test
	public void shouldParseFullWssUrl() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"url", "wss://secure.example.com/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("wss://secure.example.com/events");
		assertThat(config.getScheme()).isEqualTo("wss");
		assertThat(config.getTls().isEnabled()).isTrue();
	}

	@Test
	public void shouldThrowWhenUrlSchemeIsInvalid() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"url", "http://example.com/events"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url must start with 'ws://' or 'wss://'");
	}

	// =========================================================================
	// Host/Port/Path Configuration (Individual Properties)
	// =========================================================================

	@Test
	public void shouldBuildUrlFromHostOnly() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("ws://example.com:80/");
		assertThat(config.getScheme()).isEqualTo("ws");
		assertThat(config.getPort()).isEqualTo(80);
		assertThat(config.getPath()).isEqualTo("/");
	}

	@Test
	public void shouldBuildUrlWithCustomPort() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"port", "8080"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("ws://example.com:8080/");
		assertThat(config.getPort()).isEqualTo(8080);
	}

	@Test
	public void shouldBuildUrlWithCustomPath() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"path", "/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("ws://example.com:80/events");
		assertThat(config.getPath()).isEqualTo("/events");
	}

	@Test
	public void shouldNormalizePathWithoutLeadingSlash() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"path", "events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPath()).isEqualTo("/events");
		assertThat(config.getUrl()).isEqualTo("ws://example.com:80/events");
	}

	// =========================================================================
	// TLS Configuration
	// =========================================================================

	@Test
	public void shouldUseDefaultSecurePortWhenTlsEnabled() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getScheme()).isEqualTo("wss");
		assertThat(config.getPort()).isEqualTo(443);
		assertThat(config.getUrl()).isEqualTo("wss://example.com:443/");
	}

	// =========================================================================
	// Binary Mode
	// =========================================================================

	@Test
	public void shouldDefaultToTextMode() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isBinaryMode()).isFalse();
	}

	@Test
	public void shouldEnableBinaryMode() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"binary-mode", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isBinaryMode()).isTrue();
	}

	// =========================================================================
	// Connection Timeout
	// =========================================================================

	@Test
	public void shouldUseDefaultConnectionTimeout() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(WebSocketDestinationConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomConnectionTimeout() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"connection-timeout-seconds", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldThrowWhenConnectionTimeoutIsZero() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"connection-timeout-seconds", "0"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("connection-timeout-seconds must be greater than 0");
	}

	@Test
	public void shouldThrowWhenConnectionTimeoutIsNegative() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"connection-timeout-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("connection-timeout-seconds must be greater than 0");
	}

	// =========================================================================
	// Connection Lost Timeout (Heartbeat/Ping-Pong)
	// =========================================================================

	@Test
	public void shouldUseDefaultConnectionLostTimeout() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionLostTimeoutSeconds()).isEqualTo(WebSocketDestinationConfig.DEFAULT_CONNECTION_LOST_TIMEOUT_SECONDS);
	}

	@Test
	public void shouldUseCustomConnectionLostTimeout() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"connection-lost-timeout-seconds", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionLostTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldDisableConnectionLostTimeoutWhenSetToZero() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"connection-lost-timeout-seconds", "0"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getConnectionLostTimeoutSeconds()).isEqualTo(0);
	}

	@Test
	public void shouldThrowWhenConnectionLostTimeoutIsNegative() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"connection-lost-timeout-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("connection-lost-timeout-seconds must be non-negative");
	}

	// =========================================================================
	// Headers
	// =========================================================================

	@Test
	public void shouldParseCustomHeaders() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"headers.Authorization", "Bearer token123",
			"headers.X-Custom", "custom-value"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeaders())
			.containsEntry("Authorization", "Bearer token123")
			.containsEntry("X-Custom", "custom-value");
	}

	@Test
	public void shouldHaveEmptyHeadersByDefault() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeaders()).isEmpty();
	}

	// =========================================================================
	// Port Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenPortIsTooHigh() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
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
	public void shouldThrowWhenPortIsTooLow() {

		// arrange

		var config = new WebSocketDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"host", "example.com",
			"port", "0"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}
}
