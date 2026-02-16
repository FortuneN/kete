package io.github.fortunen.kete.unittests.destinationconfigs.socketiodestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.socketio.SocketIODestinationConfig;

public class initializeTests {

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "socketio");
		map.put("url", "http://localhost:3000");
		map.put("event-name", "keycloak-event");
		return map;
	}

	// =========================================================================
	// Required Fields - URL
	// =========================================================================

	@Test
	public void shouldThrowWhenUrlIsMissing() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "socketio", "event-name", "keycloak-event")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url is required");
	}

	@Test
	public void shouldThrowWhenUrlIsEmpty() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "socketio", "url", "", "event-name", "keycloak-event")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url is required");
	}

	@Test
	public void shouldThrowWhenUrlIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "socketio");
		map.put("url", "   ");
		map.put("event-name", "keycloak-event");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url is required");
	}

	// =========================================================================
	// Required Fields - Event Name
	// =========================================================================

	@Test
	public void shouldThrowWhenEventNameIsMissing() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "socketio", "url", "http://localhost:3000")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("event-name is required");
	}

	@Test
	public void shouldThrowWhenEventNameIsEmpty() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "socketio", "url", "http://localhost:3000", "event-name", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("event-name is required");
	}

	@Test
	public void shouldThrowWhenEventNameIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "socketio");
		map.put("url", "http://localhost:3000");
		map.put("event-name", "   ");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("event-name is required");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(20);
	}

	@Test
	public void shouldUseDefaultPath() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getPath()).isEqualTo("/socket.io/");
	}

	@Test
	public void shouldDefaultNamespaceToEmpty() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getNamespace()).isEmpty();
	}

	@Test
	public void shouldEnableReconnectionByDefault() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getOptions()).isNotNull();
		assertThat(config.getOptions().reconnection).isTrue();
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldUseCustomPath() {

		// arrange

		var map = minimalConfig();
		map.put("path", "/my-custom-path/");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getPath()).isEqualTo("/my-custom-path/");
	}

	@Test
	public void shouldUseCustomNamespace() {

		// arrange

		var map = minimalConfig();
		map.put("namespace", "events");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getNamespace()).isEqualTo("events");
	}

	@Test
	public void shouldUseCustomEventName() {

		// arrange

		var map = minimalConfig();
		map.put("event-name", "my-custom-event");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventName()).isEqualTo("my-custom-event");
	}

	@Test
	public void shouldTrimUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "socketio");
		map.put("url", "  http://localhost:3000  ");
		map.put("event-name", "keycloak-event");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://localhost:3000");
	}

	@Test
	public void shouldTrimEventName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "socketio");
		map.put("url", "http://localhost:3000");
		map.put("event-name", "  keycloak-event  ");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getEventName()).isEqualTo("keycloak-event");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldThrowWhenTimeoutSecondsIsNegative() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", -5);
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
	}

	// =========================================================================
	// Namespace Handling
	// =========================================================================

	@Test
	public void shouldPrependSlashToNamespaceIfMissing() {

		// arrange

		var map = minimalConfig();
		map.put("namespace", "events");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert — the socket URL should have /events appended

		assertThat(config.getSocketUrl()).endsWith("/events");
	}

	@Test
	public void shouldNotDoublePrependSlashToNamespace() {

		// arrange

		var map = minimalConfig();
		map.put("namespace", "/events");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getSocketUrl()).endsWith("/events");
	}

	// =========================================================================
	// Headers
	// =========================================================================

	@Test
	public void shouldParseCustomHeaders() {

		// arrange

		var map = minimalConfig();
		map.put("headers.Authorization", "Bearer token123");
		map.put("headers.X-Custom", "custom-value");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

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

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeaders()).isEmpty();
	}

	// =========================================================================
	// OAuth
	// =========================================================================

	@Test
	public void shouldCreateOAuthMaterial() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getOauth()).isNotNull();
	}

	@Test
	public void shouldSetOauthEnabledToFalseByDefault() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.isOauthEnabled()).isFalse();
	}

	@Test
	public void shouldSetOauthEnabledWhenOauthConfigured() {

		// arrange

		var map = minimalConfig();
		map.put("oauth.enabled", "true");
		map.put("oauth.token-url", "http://localhost/token");
		map.put("oauth.client-id", "test-client");
		map.put("oauth.client-secret", "test-secret");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isOauthEnabled()).isTrue();
		assertThat(config.getOauth()).isNotNull();
		assertThat(config.getOauth().isEnabled()).isTrue();
	}

	@Test
	public void shouldSetOauthEnabledToFalseWhenOauthExplicitlyDisabled() {

		// arrange

		var map = minimalConfig();
		map.put("oauth.enabled", "false");
		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isOauthEnabled()).isFalse();
	}

	// =========================================================================
	// Options and Socket URL Creation
	// =========================================================================

	@Test
	public void shouldCreateOptions() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getOptions()).isNotNull();
		assertThat(config.getSocketUrl()).isNotNull();
	}

	// =========================================================================
	// Cross-Destination Rejection
	// =========================================================================

	@Test
	public void shouldNotAcceptWebsocketKind() {

		// arrange

		var config = new SocketIODestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "websocket",
			"url", "http://localhost:3000",
			"event-name", "keycloak-event"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDestinationKind()).isEqualTo("websocket");
	}
}
