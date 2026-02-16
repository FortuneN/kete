package io.github.fortunen.kete.unittests.destinationconfigs.soapdestinationconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.soap.SoapDestinationConfig;

public class initializeTests {

	// =========================================================================
	// Required Fields - Host (when URL not provided)
	// =========================================================================

	@Test
	public void shouldThrowWhenHostIsMissing() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "soap")));

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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "http://example.com:8080/ws/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("example.com");
		assertThat(config.getPort()).isEqualTo(8080);
		assertThat(config.getScheme()).isEqualTo("http");
		assertThat(config.getPathAndQuery()).isEqualTo("/ws/events");
		assertThat(config.getUrl()).isEqualTo("http://example.com:8080/ws/events");
	}

	@Test
	public void shouldParseFullHttpsUrl() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "https://secure.example.com/ws"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("secure.example.com");
		assertThat(config.getPort()).isEqualTo(443);
		assertThat(config.getScheme()).isEqualTo("https");
		assertThat(config.getTls().isEnabled()).isTrue();
		assertThat(config.getPathAndQuery()).isEqualTo("/ws");
	}

	@Test
	public void shouldParseUrlWithQueryString() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "http://example.com/ws?wsdl=true"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPathAndQuery()).isEqualTo("/ws?wsdl=true");
	}

	@Test
	public void shouldUseDefaultPortForHttp() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "http://example.com/ws"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(80);
	}

	@Test
	public void shouldUseDefaultPortForHttps() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "https://example.com/ws"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPort()).isEqualTo(443);
	}

	@Test
	public void shouldThrowWhenUrlSchemeIsInvalid() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "ftp://example.com/ws"
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"path-and-query", "/ws/events?source=keycloak"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getUrl()).isEqualTo("http://example.com:80/ws/events?source=keycloak");
		assertThat(config.getPathAndQuery()).isEqualTo("/ws/events?source=keycloak");
	}

	@Test
	public void shouldPrependSlashToPath() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"path-and-query", "ws/events"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getPathAndQuery()).isEqualTo("/ws/events");
	}

	@Test
	public void shouldUseHttpsWhenTlsEnabled() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

	// =========================================================================
	// Timeout
	// =========================================================================

	@Test
	public void shouldDefaultToTenSecondsTimeout() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

	// =========================================================================
	// Authentication
	// =========================================================================

	@Test
	public void shouldConfigureBasicAuth() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"authentication-type", "basic",
			"basic-username", "user",
			"basic-password", "pass"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getAuthHeaderName()).isEqualTo("Authorization");
		assertThat(config.getAuthHeaderValue()).startsWith("Basic ");
	}

	@Test
	public void shouldConfigureApiKeyAuth() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"authentication-type", "api-key",
			"api-key-value", "my-secret-key"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getAuthHeaderName()).isEqualTo("Api-Key");
		assertThat(config.getAuthHeaderValue()).isEqualTo("my-secret-key");
	}

	@Test
	public void shouldConfigureXApiKeyAuth() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"authentication-type", "x-api-key",
			"x-api-key-value", "my-secret-key"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getAuthHeaderName()).isEqualTo("X-API-Key");
		assertThat(config.getAuthHeaderValue()).isEqualTo("my-secret-key");
	}

	@Test
	public void shouldThrowWhenAuthenticationTypeIsInvalid() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"authentication-type", "invalid"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("authentication-type must be one of: oauth, basic, api-key, x-api-key");
	}

	// =========================================================================
	// Custom Headers
	// =========================================================================

	@Test
	public void shouldParseCustomHeaders() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"headers.X-Custom-Header", "custom-value"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getCustomHeaders()).containsEntry("X-Custom-Header", "custom-value");
	}

	// =========================================================================
	// TLS
	// =========================================================================

	@Test
	public void shouldDefaultToTlsDisabled() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "https://example.com/ws"
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

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"url", "http://from-url.com:9090/ws",
			"host", "from-host.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("from-url.com");
		assertThat(config.getPort()).isEqualTo(9090);
		assertThat(config.getPathAndQuery()).isEqualTo("/ws");
	}

	// =========================================================================
	// SOAP Version
	// =========================================================================

	@Test
	public void shouldDefaultSoapVersionTo11() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSoapVersion()).isEqualTo("1.1");
		assertThat(config.getSoapNamespace()).isEqualTo("http://schemas.xmlsoap.org/soap/envelope/");
		assertThat(config.getSoapContentType()).isEqualTo("text/xml");
	}

	@Test
	public void shouldAcceptSoapVersion12() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"soap-version", "1.2"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSoapVersion()).isEqualTo("1.2");
		assertThat(config.getSoapNamespace()).isEqualTo("http://www.w3.org/2003/05/soap-envelope");
		assertThat(config.getSoapContentType()).isEqualTo("application/soap+xml");
	}

	@Test
	public void shouldAcceptSoapVersion11Explicitly() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"soap-version", "1.1"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSoapVersion()).isEqualTo("1.1");
		assertThat(config.getSoapNamespace()).isEqualTo("http://schemas.xmlsoap.org/soap/envelope/");
		assertThat(config.getSoapContentType()).isEqualTo("text/xml");
	}

	@Test
	public void shouldThrowWhenSoapVersionIsInvalid() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"soap-version", "2.0"
		)));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("soap-version must be '1.1' or '1.2'");
	}

	// =========================================================================
	// SOAP Action
	// =========================================================================

	@Test
	public void shouldDefaultSoapActionToNull() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSoapAction()).isNull();
	}

	@Test
	public void shouldAcceptSoapAction() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of(
			"kind", "soap",
			"host", "example.com",
			"soap-action", "urn:processKeycloakEvent"
		)));

		// act

		config.initialize();

		// assert

		assertThat(config.getSoapAction()).isEqualTo("urn:processKeycloakEvent");
	}

	// =========================================================================
	// Full Configuration
	// =========================================================================

	@Test
	public void shouldInitializeWithFullConfiguration() {

		// arrange

		var configMap = new HashMap<String, Object>();
		configMap.put("kind", "soap");
		configMap.put("host", "soap.example.com");
		configMap.put("port", "8443");
		configMap.put("path-and-query", "/ws/events");
		configMap.put("timeout-seconds", "30");
		configMap.put("tls.enabled", "true");
		configMap.put("soap-version", "1.2");
		configMap.put("soap-action", "urn:processEvent");
		configMap.put("headers.X-Custom", "value");

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(configMap));

		// act

		config.initialize();

		// assert

		assertThat(config.getHost()).isEqualTo("soap.example.com");
		assertThat(config.getPort()).isEqualTo(8443);
		assertThat(config.getPathAndQuery()).isEqualTo("/ws/events");
		assertThat(config.getTimeoutSeconds()).isEqualTo(30);
		assertThat(config.getScheme()).isEqualTo("https");
		assertThat(config.getTls().isEnabled()).isTrue();
		assertThat(config.getSoapVersion()).isEqualTo("1.2");
		assertThat(config.getSoapNamespace()).isEqualTo("http://www.w3.org/2003/05/soap-envelope");
		assertThat(config.getSoapContentType()).isEqualTo("application/soap+xml");
		assertThat(config.getSoapAction()).isEqualTo("urn:processEvent");
		assertThat(config.getUrl()).isEqualTo("https://soap.example.com:8443/ws/events");
		assertThat(config.getCustomHeaders()).containsEntry("X-Custom", "value");
	}

	// =========================================================================
	// content-transfer-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentTransferEncodingToNull() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "soap", "host", "example.com")));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNull();
		assertThat(config.getContentTransferEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentTransferEncoding() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "soap", "host", "example.com", "content-transfer-encoding", "base64")));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentTransferEncoding()).isNotNull();
		assertThat(config.getContentTransferEncodingName()).isEqualTo("base64");
	}

	@Test
	public void shouldThrowWhenContentTransferEncodingIsUnknown() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "soap", "host", "example.com", "content-transfer-encoding", "unknown")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-transfer-encoding: unknown");
	}

	// =========================================================================
	// content-encoding
	// =========================================================================

	@Test
	public void shouldDefaultContentEncodingToNull() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "soap", "host", "example.com")));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNull();
		assertThat(config.getContentEncodingName()).isNull();
	}

	@Test
	public void shouldResolveContentEncoding() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "soap", "host", "example.com", "content-encoding", "gzip")));

		// act

		config.initialize();

		// assert

		assertThat(config.getContentEncoding()).isNotNull();
		assertThat(config.getContentEncodingName()).isEqualTo("gzip");
	}

	@Test
	public void shouldThrowWhenContentEncodingIsUnknown() {

		// arrange

		var config = new SoapDestinationConfig();
		config.setConfiguration(new MapConfiguration(Map.of("kind", "soap", "host", "example.com", "content-encoding", "unknown")));

		// act

		var thrown = catchThrowable(() -> config.initialize());

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("unknown content-encoding: unknown");
	}
}
