package io.github.fortunen.kete.unittests.destinationconfigs.signalrdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.signalr.SignalRDestinationConfig;

public class initializeTests {

	private Map<String, Object> minimalConfig() {
		var map = new HashMap<String, Object>();
		map.put("kind", "signalr");
		map.put("url", "http://localhost:5000/eventHub");
		return map;
	}

	// =========================================================================
	// Required Fields - URL
	// =========================================================================

	@Test
	public void shouldThrowWhenUrlIsMissing() {

		// arrange

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "signalr")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url is required");
	}

	@Test
	public void shouldThrowWhenUrlIsEmpty() {

		// arrange

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "signalr", "url", "")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url is required");
	}

	@Test
	public void shouldThrowWhenUrlIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "signalr");
		map.put("url", "   ");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url is required");
	}

	// =========================================================================
	// Defaults
	// =========================================================================

	@Test
	public void shouldUseDefaultHubMethod() {

		// arrange

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getHubMethod()).isEqualTo("SendEvent");
	}

	@Test
	public void shouldUseDefaultTimeoutSeconds() {

		// arrange

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
	}

	@Test
	public void shouldDefaultAccessTokenToEmpty() {

		// arrange

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getAccessToken()).isEmpty();
	}

	// =========================================================================
	// Custom Configuration
	// =========================================================================

	@Test
	public void shouldUseCustomHubMethod() {

		// arrange

		var map = minimalConfig();
		map.put("hub-method", "BroadcastEvent");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHubMethod()).isEqualTo("BroadcastEvent");
	}

	@Test
	public void shouldUseCustomTimeoutSeconds() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 30);
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldUseCustomAccessToken() {

		// arrange

		var map = minimalConfig();
		map.put("access-token", "my-secret-token");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAccessToken()).isEqualTo("my-secret-token");
	}

	@Test
	public void shouldTrimUrl() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("kind", "signalr");
		map.put("url", "  http://localhost:5000/eventHub  ");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://localhost:5000/eventHub");
	}

	@Test
	public void shouldTrimHubMethod() {

		// arrange

		var map = minimalConfig();
		map.put("hub-method", "  BroadcastEvent  ");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getHubMethod()).isEqualTo("BroadcastEvent");
	}

	@Test
	public void shouldTrimAccessToken() {

		// arrange

		var map = minimalConfig();
		map.put("access-token", "  my-token  ");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.getAccessToken()).isEqualTo("my-token");
	}

	// =========================================================================
	// Validation - Hub Method
	// =========================================================================

	@Test
	public void shouldThrowWhenHubMethodIsBlank() {

		// arrange

		var map = minimalConfig();
		map.put("hub-method", "   ");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("hub-method must not be blank");
	}

	// =========================================================================
	// Validation - Timeout
	// =========================================================================

	@Test
	public void shouldThrowWhenTimeoutSecondsIsZero() {

		// arrange

		var map = minimalConfig();
		map.put("timeout-seconds", 0);
		var config = new SignalRDestinationConfig();
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
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("timeout-seconds must be positive");
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
		var config = new SignalRDestinationConfig();
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

		var config = new SignalRDestinationConfig();
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

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getOauth()).isNotNull();
	}

	@Test
	public void shouldSetOauthEnabledToFalseByDefault() {

		// arrange

		var config = new SignalRDestinationConfig();
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
		var config = new SignalRDestinationConfig();
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
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isOauthEnabled()).isFalse();
	}

	@Test
	public void shouldPreferOAuthOverAccessToken() {

		// arrange

		var map = minimalConfig();
		map.put("access-token", "static-token");
		map.put("oauth.enabled", "true");
		map.put("oauth.token-url", "http://localhost/token");
		map.put("oauth.client-id", "test-client");
		map.put("oauth.client-secret", "test-secret");
		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(map));

		// act

		config.initialize();

		// assert

		assertThat(config.isOauthEnabled()).isTrue();
		assertThat(config.getAccessToken()).isEqualTo("static-token");
	}

	// =========================================================================
	// Builder
	// =========================================================================

	@Test
	public void shouldCreateHubConnectionBuilder() {

		// arrange

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(minimalConfig()));

		// act

		config.initialize();

		// assert

		assertThat(config.getHubConnectionBuilder()).isNotNull();
	}

	// =========================================================================
	// Cross-Destination Rejection
	// =========================================================================

	@Test
	public void shouldNotAcceptWebsocketKind() {

		// arrange

		var config = new SignalRDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "websocket",
			"url", "http://localhost:5000/eventHub"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getDestinationKind()).isEqualTo("websocket");
	}
}
