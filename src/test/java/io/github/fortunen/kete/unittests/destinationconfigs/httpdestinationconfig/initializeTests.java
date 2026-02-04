package io.github.fortunen.kete.unittests.destinationconfigs.httpdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.http.HttpDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host (when URL not provided)
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "http")));

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

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
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

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
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
	public void shouldParseFullHttpUrl() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http://example.com:8080/api/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("example.com");
		assertThat(config.getPort()).isEqualTo(8080);
		assertThat(config.getScheme()).isEqualTo("http");
		assertThat(config.getPathAndQuery()).isEqualTo("/api/events");
		assertThat(config.getUrl()).isEqualTo("http://example.com:8080/api/events");
	}

	@Test
	public void shouldParseFullHttpsUrl() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "https://secure.example.com/webhook"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("secure.example.com");
		assertThat(config.getPort()).isEqualTo(443);
		assertThat(config.getScheme()).isEqualTo("https");
		assertThat(config.getTls().isEnabled()).isTrue();
		assertThat(config.getPathAndQuery()).isEqualTo("/webhook");
	}

	@Test
	public void shouldParseUrlWithQueryString() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http://example.com/api?key=value&foo=bar"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPathAndQuery()).isEqualTo("/api?key=value&foo=bar");
	}

	@Test
	public void shouldUseDefaultPortForHttp() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http://example.com/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(80);
	}

	@Test
	public void shouldUseDefaultPortForHttps() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "https://example.com/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(443);
	}

	@Test
	public void shouldThrowWhenUrlSchemeIsInvalid() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "ftp://example.com/files"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url scheme must be 'http' or 'https'");
	}

	@Test
	public void shouldThrowWhenUrlHostIsMissing() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http:///path"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url must contain a valid host");
	}

	@Test
	public void shouldUseRootPathWhenUrlHasNoPath() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http://example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPathAndQuery()).isEqualTo("/");
		assertThat(config.getUrl()).isEqualTo("http://example.com:80");
	}

	// =========================================================================
	// Host/Port/Path Configuration (Individual Properties)
	// =========================================================================

	@Test
	public void shouldBuildUrlFromHostOnly() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://example.com:80");
		assertThat(config.getScheme()).isEqualTo("http");
		assertThat(config.getPort()).isEqualTo(80);
		assertThat(config.getPathAndQuery()).isEqualTo("/");
	}

	@Test
	public void shouldBuildUrlWithCustomPort() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"port", "8080"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://example.com:8080");
		assertThat(config.getPort()).isEqualTo(8080);
	}

	@Test
	public void shouldBuildUrlWithPathAndQuery() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"path-and-query", "/api/events?source=keycloak"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://example.com:80/api/events?source=keycloak");
		assertThat(config.getPathAndQuery()).isEqualTo("/api/events?source=keycloak");
	}

	@Test
	public void shouldPrependSlashToPath() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"path-and-query", "api/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPathAndQuery()).isEqualTo("/api/events");
	}

	@Test
	public void shouldUseHttpsWhenTlsEnabled() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getScheme()).isEqualTo("https");
		assertThat(config.getPort()).isEqualTo(443);
		assertThat(config.getUrl()).isEqualTo("https://example.com:443");
	}

	// =========================================================================
	// Port Validation
	// =========================================================================

	@Test
	public void shouldThrowWhenPortIsZero() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
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

	@Test
	public void shouldThrowWhenPortIsNegative() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
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

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
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
	public void shouldAcceptMinimumPort() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
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

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"port", "65535"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(65535);
	}

	// =========================================================================
	// HTTP Method
	// =========================================================================

	@Test
	public void shouldDefaultToPostMethod() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getMethod()).isEqualTo("POST");
		assertThat(config.isMethodIsPost()).isTrue();
	}

	@Test
	public void shouldAcceptPutMethod() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"method", "PUT"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getMethod()).isEqualTo("PUT");
		assertThat(config.isMethodIsPost()).isFalse();
	}

	@Test
	public void shouldNormalizeMethodToUpperCase() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"method", "post"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getMethod()).isEqualTo("POST");
	}

	@Test
	public void shouldThrowWhenMethodIsInvalid() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"method", "GET"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("method must be either POST or PUT");
	}

	@Test
	public void shouldThrowWhenMethodIsDelete() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"method", "DELETE"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("method must be either POST or PUT");
	}

	// =========================================================================
	// Timeout
	// =========================================================================

	@Test
	public void shouldDefaultToTenSecondsTimeout() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(10);
	}

	@Test
	public void shouldAcceptCustomTimeout() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"timeout-seconds", "30"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
	}

	@Test
	public void shouldThrowWhenTimeoutIsZero() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"timeout-seconds", "0"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldThrowWhenTimeoutIsNegative() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"timeout-seconds", "-1"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("timeout-seconds must be positive");
	}

	@Test
	public void shouldAcceptMinimumTimeout() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"timeout-seconds", "1"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTimeoutSeconds()).isEqualTo(1);
	}

	// =========================================================================
	// Custom Headers
	// =========================================================================

	@Test
	public void shouldParseCustomHeaders() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"headers.X-Custom-Header", "custom-value",
			"headers.Authorization", "Bearer token123"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeaders()).containsEntry("X-Custom-Header", "custom-value");
		assertThat(config.getCustomHeaders()).containsEntry("Authorization", "Bearer token123");
	}

	@Test
	public void shouldIgnoreEmptyHeaders() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "http");
		configMap.put("host", "example.com");
		configMap.put("headers.Empty-Header", "");
		configMap.put("headers.Valid-Header", "value");

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeaders()).doesNotContainKey("Empty-Header");
		assertThat(config.getCustomHeaders()).containsEntry("Valid-Header", "value");
	}

	@Test
	public void shouldTrimHeaderValues() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"headers.Trimmed", "  value with spaces  "
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeaders()).containsEntry("Trimmed", "value with spaces");
	}

	// =========================================================================
	// OAuth
	// =========================================================================

	@Test
	public void shouldCreateOAuthMaterial() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getOauth()).isNotNull();
	}

	@Test
	public void shouldSetOauthEnabledToFalseByDefault() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.isOauthEnabled()).isFalse();
	}

	@Test
	public void shouldSetOauthEnabledWhenOauthConfigured() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "http");
		configMap.put("host", "example.com");
		configMap.put("oauth.enabled", "true");
		configMap.put("oauth.token-url", "http://localhost/token");
		configMap.put("oauth.client-id", "test-client");
		configMap.put("oauth.client-secret", "test-secret");

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

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

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "http");
		configMap.put("host", "example.com");
		configMap.put("oauth.enabled", "false");

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.isOauthEnabled()).isFalse();
	}

	// =========================================================================
	// TLS
	// =========================================================================

	@Test
	public void shouldDefaultToTlsDisabled() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isFalse();
	}

	@Test
	public void shouldEnableTls() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"host", "example.com",
			"tls.enabled", "true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
	}

	@Test
	public void shouldAutoEnableTlsFromHttpsUrl() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "https://example.com/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getTls().isEnabled()).isTrue();
	}

	// =========================================================================
	// URL Precedence Over Individual Properties
	// =========================================================================

	@Test
	public void shouldUseUrlOverHostWhenBothProvided() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http://from-url.com:9090/path",
			"host", "from-host.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("from-url.com");
		assertThat(config.getPort()).isEqualTo(9090);
		assertThat(config.getPathAndQuery()).isEqualTo("/path");
	}

	@Test
	public void shouldUseUrlOverPortWhenBothProvided() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http://example.com:9090",
			"port", "8080"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(9090);
	}

	@Test
	public void shouldUseUrlOverPathWhenBothProvided() {

		// arrange

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "http",
			"url", "http://example.com/from-url",
			"path-and-query", "/from-config"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPathAndQuery()).isEqualTo("/from-url");
	}

	// =========================================================================
	// Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithFullConfiguration() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "http");
		configMap.put("host", "api.example.com");
		configMap.put("port", "8443");
		configMap.put("path-and-query", "/events/keycloak?source=auth");
		configMap.put("method", "PUT");
		configMap.put("timeout-seconds", "30");
		configMap.put("headers.X-API-Key", "secret-key");
		configMap.put("tls.enabled", "true");

		var config = new HttpDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("api.example.com");
		assertThat(config.getPort()).isEqualTo(8443);
		assertThat(config.getPathAndQuery()).isEqualTo("/events/keycloak?source=auth");
		assertThat(config.getMethod()).isEqualTo("PUT");
		assertThat(config.isMethodIsPost()).isFalse();
		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getCustomHeaders()).containsEntry("X-API-Key", "secret-key");
		assertThat(config.getScheme()).isEqualTo("https");
		assertThat(config.getTls().isEnabled()).isTrue();
		assertThat(config.getUrl()).isEqualTo("https://api.example.com:8443/events/keycloak?source=auth");
		assertThat(config.getOauth()).isNotNull();
	}
}
