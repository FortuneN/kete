package io.github.fortunen.kete.destinations.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class HttpDestinationConfig extends DestinationConfig {

	public static final String URL = "url";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final int DEFAULT_HTTP_PORT = 80;
	public static final int DEFAULT_HTTPS_PORT = 443;
	public static final String PATH_AND_QUERY = "path-and-query";

	public static final String OAUTH = "oauth";

	public static final String PUT = "PUT";
	public static final String POST = "POST";
	public static final String METHOD = "method";

	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String TIMEOUT_SECONDS = "timeout-seconds";

	private int port;
	private String url;
	private String host;
	private String path;
	private String query;
	private String scheme;
	private String method;
	private URI parsedUri;
	private boolean hasHost;
	private boolean hasPort;
	private boolean hasPath;
	private boolean tlsFromUrl;
	private int timeoutSeconds;
	private String pathAndQuery;
	private OAuthMaterial oauth;
	private String urlFromConfig;
	private boolean methodIsPost;
	private boolean hasTlsEnabled;
	private boolean oauthEnabled;
	private MapConfiguration tlsConfig;
	private HttpClient.Builder clientBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// url

		urlFromConfig = configuration.getString(URL, "").trim();

		if (ValidationUtils.isNotBlank(urlFromConfig)) {

			parsedUri = URI.create(urlFromConfig);
			hasPort = configuration.containsKey(PORT);
			hasHost = ValidationUtils.isNotBlank(configuration.getString(HOST, "").trim());
			hasTlsEnabled = ConfigurationUtils.getSubSet(configuration, TLS).containsKey("enabled");
			hasPath = ValidationUtils.isNotBlank(configuration.getString(PATH_AND_QUERY, "").trim());

			if (hasHost || hasPort || hasPath || hasTlsEnabled) {
				log.warn("Both 'url' and individual properties (host/port/path-and-query/tls.enabled) are specified. The 'url' property takes precedence and overrides: " + (hasHost ? "host " : "") + (hasPort ? "port " : "") + (hasPath ? "path-and-query " : "") + (hasTlsEnabled ? "tls.enabled" : ""));
			}

			// scheme

			scheme = parsedUri.getScheme();

			ValidationUtils.requireTrue("http".equals(scheme) || "https".equals(scheme), "url scheme must be 'http' or 'https'");

			// tls

			tlsFromUrl = "https".equals(scheme);

			if (tlsFromUrl != tls.isEnabled()) {
				tlsConfig = ConfigurationUtils.getSubSet(configuration, TLS);
				tlsConfig.getMap().put("enabled", String.valueOf(tlsFromUrl));
				tls = TlsMaterial.builder().withConfiguration(tlsConfig).build();
			}

			// host

			host = ValidationUtils.requireNonBlank(parsedUri.getHost(), "url must contain a valid host");

			// port

			port = parsedUri.getPort();

			if (port <= 0) {
				port = tlsFromUrl ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
			}

			// pathAndQuery

			path = parsedUri.getRawPath();
			query = parsedUri.getRawQuery();

			if (ValidationUtils.isBlank(path)) {
				pathAndQuery = "/";
			} else {
				pathAndQuery = path;
			}

			if (ValidationUtils.isNotBlank(query)) {
				pathAndQuery = pathAndQuery + "?" + query;
			}

		} else {

			// host

			host = ValidationUtils.requireNonBlank(configuration.getString(HOST, "").trim(), HOST + " is required");

			// scheme

			scheme = tls.isEnabled() ? "https" : "http";

			// port

			port = configuration.getInt(PORT, tls.isEnabled() ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT);

			ValidationUtils.requireValidPort(port, PORT);

			// pathAndQuery

			pathAndQuery = configuration.getString(PATH_AND_QUERY, "").trim();

			if (ValidationUtils.isBlank(pathAndQuery)) {
				pathAndQuery = "/";
			}

			pathAndQuery = pathAndQuery.trim();

			if (!pathAndQuery.startsWith("/")) {
				pathAndQuery = "/" + pathAndQuery;
			}
		}

		// baseUrl

		if ("/".equals(pathAndQuery)) {
			url = scheme + "://" + host + ":" + port;
		} else {
			url = scheme + "://" + host + ":" + port + pathAndQuery;
		}

		// method

		method = configuration.getString(METHOD, POST).trim().toUpperCase();

		ValidationUtils.requireTrue(method.equals(POST) || method.equals(PUT), METHOD + " must be either POST or PUT");

		methodIsPost = method.equals(POST);

		// timeoutSeconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// oauth

		oauth = OAuthMaterial.builder().withKeycloakRealm(keycloakRealm).withKeycloakSession(keycloakSession).withConfiguration(ConfigurationUtils.getSubSet(configuration, OAUTH)).build();

		oauthEnabled = ValidationUtils.isNotNull(oauth) && oauth.isEnabled();

		// clientBuilder

		clientBuilder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSeconds));

		if (tls.isEnabled()) {
			clientBuilder.sslContext(tls.getKeyStoreAndTrustStoreSSLContext());
		}
	}
}
