package io.github.fortunen.kete.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.MapConfiguration;
import org.keycloak.models.KeycloakSession;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.TlsMaterial;
import lombok.extern.slf4j.Slf4j;

// the HTTP-based destinations (HTTP, SOAP) share one way of resolving their endpoint, authentication and client

@Slf4j
public final class HttpEndpointUtils {

	private HttpEndpointUtils() {}

	public static final String URL = "url";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String OAUTH = "oauth";
	public static final int DEFAULT_HTTP_PORT = 80;
	public static final int DEFAULT_HTTPS_PORT = 443;
	public static final String API_KEY_VALUE = "api-key-value";
	public static final String BASIC_USERNAME = "basic-username";
	public static final String BASIC_PASSWORD = "basic-password";
	public static final String PATH_AND_QUERY = "path-and-query";
	public static final String X_API_KEY_VALUE = "x-api-key-value";

	public record Endpoint(String url, String urlFromConfig, URI parsedUri, String scheme, String host, int port, String path, String query, String pathAndQuery,
		boolean hasHost, boolean hasPort, boolean hasPath, boolean hasTlsEnabled, boolean tlsFromUrl, MapConfiguration tlsConfig, TlsMaterial tls, boolean urlTemplated) {}

	public record Authentication(String headerName, String headerValue, OAuthMaterial oauth, boolean oauthEnabled) {}

	// `url` wins over host/port/path-and-query/tls.enabled; an https url turns TLS on (and http turns it off) by rebuilding the TLS material

	public static Endpoint resolveEndpoint(MapConfiguration configuration, TlsMaterial tls) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");
		ValidationUtils.requireNonNull(tls, "tls is required");

		var urlFromConfig = configuration.getString(URL, "").trim();

		URI parsedUri = null;
		MapConfiguration tlsConfig = null;
		var hasHost = false;
		var hasPort = false;
		var hasPath = false;
		var hasTlsEnabled = false;
		var tlsFromUrl = false;
		String path = null;
		String query = null;
		String scheme;
		String host;
		String pathAndQuery;
		int port;

		if (ValidationUtils.isNotBlank(urlFromConfig)) {

			parsedUri = URI.create(TemplateUtils.maskTemplates(urlFromConfig));
			hasPort = configuration.containsKey(PORT);
			hasHost = ValidationUtils.isNotBlank(configuration.getString(HOST, "").trim());
			hasTlsEnabled = ConfigurationUtils.getSubSet(configuration, DestinationConfig.TLS).containsKey("enabled");
			hasPath = ValidationUtils.isNotBlank(configuration.getString(PATH_AND_QUERY, "").trim());

			if (hasHost || hasPort || hasPath || hasTlsEnabled) {
				log.warn("Both 'url' and individual properties (host/port/path-and-query/tls.enabled) are specified. The 'url' property takes precedence and overrides: " + (hasHost ? "host " : "") + (hasPort ? "port " : "") + (hasPath ? "path-and-query " : "") + (hasTlsEnabled ? "tls.enabled" : ""));
			}

			scheme = parsedUri.getScheme();

			ValidationUtils.requireTrue("http".equals(scheme) || "https".equals(scheme), "url scheme must be 'http' or 'https'");

			tlsFromUrl = "https".equals(scheme);

			if (tlsFromUrl != tls.isEnabled()) {
				tlsConfig = ConfigurationUtils.getSubSet(configuration, DestinationConfig.TLS);
				tlsConfig.getMap().put("enabled", String.valueOf(tlsFromUrl));
				tls = TlsMaterial.builder().withConfiguration(tlsConfig).build();
			}

			host = ValidationUtils.requireNonBlank(parsedUri.getHost(), "url must contain a valid host");

			port = parsedUri.getPort();

			if (port <= 0) {
				port = tlsFromUrl ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
			}

			path = parsedUri.getRawPath();
			query = parsedUri.getRawQuery();

			pathAndQuery = ValidationUtils.isBlank(path) ? "/" : path;

			if (ValidationUtils.isNotBlank(query)) {
				pathAndQuery = pathAndQuery + "?" + query;
			}

		} else {

			host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");
			scheme = tls.isEnabled() ? "https" : "http";
			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT);

			ValidationUtils.requireValidPort(port, PORT);

			pathAndQuery = configuration.getString(PATH_AND_QUERY, "").trim();

			if (ValidationUtils.isBlank(pathAndQuery)) {
				pathAndQuery = "/";
			}

			if (!pathAndQuery.startsWith("/")) {
				pathAndQuery = "/" + pathAndQuery;
			}
		}

		var url = "/".equals(pathAndQuery) ? scheme + "://" + host + ":" + port : scheme + "://" + host + ":" + port + pathAndQuery;

		// a templated url keeps its placeholders (they are substituted per event)

		if (TemplateUtils.containsTemplate(urlFromConfig)) {
			url = urlFromConfig;
		}

		return new Endpoint(url, urlFromConfig, parsedUri, scheme, host, port, path, query, pathAndQuery, hasHost, hasPort, hasPath, hasTlsEnabled, tlsFromUrl, tlsConfig, tls, TemplateUtils.containsTemplate(url));
	}

	public static Authentication resolveAuthentication(MapConfiguration configuration, String authenticationType, String keycloakRealm, KeycloakSession keycloakSession) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		if (ValidationUtils.isBlank(authenticationType)) {
			return new Authentication(null, null, null, false);
		}

		return switch (authenticationType) {
			case "oauth" -> {
				var oauth = OAuthMaterial.builder().withKeycloakRealm(keycloakRealm).withKeycloakSession(keycloakSession).withConfiguration(ConfigurationUtils.getSubSet(configuration, OAUTH)).build();
				yield new Authentication(null, null, oauth, ValidationUtils.isNotNull(oauth) && oauth.isEnabled());
			}
			case "basic" -> {
				var username = ValidationUtils.requireNonBlank(configuration.getString(BASIC_USERNAME, "").trim(), BASIC_USERNAME + " is required when authentication-type is 'basic'");
				var password = ValidationUtils.requireNonBlank(configuration.getString(BASIC_PASSWORD, "").trim(), BASIC_PASSWORD + " is required when authentication-type is 'basic'");
				yield new Authentication("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)), null, false);
			}
			case "api-key" -> new Authentication("Api-Key", ValidationUtils.requireNonBlank(configuration.getString(API_KEY_VALUE, "").trim(), API_KEY_VALUE + " is required when authentication-type is 'api-key'"), null, false);
			case "x-api-key" -> new Authentication("X-API-Key", ValidationUtils.requireNonBlank(configuration.getString(X_API_KEY_VALUE, "").trim(), X_API_KEY_VALUE + " is required when authentication-type is 'x-api-key'"), null, false);
			default -> throw new IllegalStateException(DestinationConfig.AUTHENTICATION_TYPE + " must be one of: oauth, basic, api-key, x-api-key");
		};
	}

	// the destination sets Content-Type and the x-event headers itself; user-supplied duplicates are dropped

	public static Set<Map.Entry<String, String>> filterCustomHeaders(Set<Map.Entry<String, String>> customHeaders) {

		ValidationUtils.requireNonNull(customHeaders, "customHeaders is required");

		var contentTypeHeader = "Content-Type";
		var eventKindHeader = "x-" + Constants.MESSAGE_HEADER_EVENT_KIND;
		var eventTypeHeader = "x-" + Constants.MESSAGE_HEADER_EVENT_TYPE;

		return customHeaders.stream()
			.filter(entry -> {
				var key = entry.getKey();
				return !contentTypeHeader.equalsIgnoreCase(key) && !eventKindHeader.equalsIgnoreCase(key) && !eventTypeHeader.equalsIgnoreCase(key);
			})
			.collect(Collectors.toSet());
	}

	public static HttpClient.Builder createClientBuilder(int timeoutSeconds, TlsMaterial tls) {

		ValidationUtils.requirePositive(timeoutSeconds, "timeoutSeconds must be positive");
		ValidationUtils.requireNonNull(tls, "tls is required");

		var clientBuilder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSeconds));

		if (tls.isEnabled()) {
			clientBuilder.sslContext(tls.getKeyStoreAndTrustStoreSSLContext());
		}

		return clientBuilder;
	}
}
