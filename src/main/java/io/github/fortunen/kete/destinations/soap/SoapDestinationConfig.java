package io.github.fortunen.kete.destinations.soap;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
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
public class SoapDestinationConfig extends DestinationConfig {

	public static final String URL = "url";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String OAUTH = "oauth";
	public static final int DEFAULT_HTTP_PORT = 80;
	public static final int DEFAULT_HTTPS_PORT = 443;
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String API_KEY_VALUE = "api-key-value";
	public static final String BASIC_USERNAME = "basic-username";
	public static final String BASIC_PASSWORD = "basic-password";
	public static final String PATH_AND_QUERY = "path-and-query";
	public static final String X_API_KEY_VALUE = "x-api-key-value";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String SOAP_ACTION = "soap-action";
	public static final String SOAP_VERSION = "soap-version";

	public static final String SOAP_11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String SOAP_12_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
	public static final String SOAP_11_CONTENT_TYPE = "text/xml";
	public static final String SOAP_12_CONTENT_TYPE = "application/soap+xml";

	private int port;
	private String url;
	private String host;
	private String path;
	private String query;
	private String scheme;
	private URI parsedUri;
	private boolean hasHost;
	private boolean hasPort;
	private boolean hasPath;
	private Duration timeout;
	private boolean tlsFromUrl;
	private int timeoutSeconds;
	private String pathAndQuery;
	private OAuthMaterial oauth;
	private String urlFromConfig;
	private boolean oauthEnabled;
	private boolean hasTlsEnabled;
	private String authHeaderName;
	private boolean isUrlTemplated;
	private String authHeaderValue;
	private MapConfiguration tlsConfig;
	private String soapAction;
	private String soapVersion;
	private String soapNamespace;
	private String soapContentType;
	private HttpClient.Builder clientBuilder;
	private Set<Map.Entry<String, String>> filteredCustomHeaders;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// url

		urlFromConfig = configuration.getString(URL, "").trim();

		if (ValidationUtils.isNotBlank(urlFromConfig)) {

			parsedUri = URI.create(TemplateUtils.maskTemplates(urlFromConfig));
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

		// a templated url keeps its placeholders (they are substituted per event)

		if (TemplateUtils.containsTemplate(urlFromConfig)) {
			url = urlFromConfig;
		}

		// timeoutSeconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// authentication-type

		if (hasAuthenticationType) {
			switch (authenticationType) {
				case "oauth" -> {
					oauth = OAuthMaterial.builder().withKeycloakRealm(keycloakRealm).withKeycloakSession(keycloakSession).withConfiguration(ConfigurationUtils.getSubSet(configuration, OAUTH)).build();
					oauthEnabled = ValidationUtils.isNotNull(oauth) && oauth.isEnabled();
				}
				case "basic" -> {
					var username = ValidationUtils.requireNonBlank(configuration.getString(BASIC_USERNAME, "").trim(), BASIC_USERNAME + " is required when authentication-type is 'basic'");
					var password = ValidationUtils.requireNonBlank(configuration.getString(BASIC_PASSWORD, "").trim(), BASIC_PASSWORD + " is required when authentication-type is 'basic'");
					authHeaderName = "Authorization";
					authHeaderValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
				}
				case "api-key" -> {
					authHeaderName = "Api-Key";
					authHeaderValue = ValidationUtils.requireNonBlank(configuration.getString(API_KEY_VALUE, "").trim(), API_KEY_VALUE + " is required when authentication-type is 'api-key'");
				}
				case "x-api-key" -> {
					authHeaderName = "X-API-Key";
					authHeaderValue = ValidationUtils.requireNonBlank(configuration.getString(X_API_KEY_VALUE, "").trim(), X_API_KEY_VALUE + " is required when authentication-type is 'x-api-key'");
				}
				default -> throw new IllegalStateException(AUTHENTICATION_TYPE + " must be one of: oauth, basic, api-key, x-api-key");
			}
		}

		// precomputed fields

		isUrlTemplated = TemplateUtils.containsTemplate(url);
		timeout = Duration.ofSeconds(timeoutSeconds);

		var contentTypeHeader = "Content-Type";
		var eventKindHeader = "x-" + Constants.MESSAGE_HEADER_EVENT_KIND;
		var eventTypeHeader = "x-" + Constants.MESSAGE_HEADER_EVENT_TYPE;

		filteredCustomHeaders = customHeadersEntrySet.stream()
			.filter(entry -> {
				var key = entry.getKey();
				return !contentTypeHeader.equalsIgnoreCase(key) && !eventKindHeader.equalsIgnoreCase(key) && !eventTypeHeader.equalsIgnoreCase(key);
			})
			.collect(Collectors.toSet());

		// soap-version

		soapVersion = configuration.getString(SOAP_VERSION, "1.1").trim();

		ValidationUtils.requireTrue("1.1".equals(soapVersion) || "1.2".equals(soapVersion), SOAP_VERSION + " must be '1.1' or '1.2'");

		if ("1.2".equals(soapVersion)) {
			soapNamespace = SOAP_12_NAMESPACE;
			soapContentType = SOAP_12_CONTENT_TYPE;
		} else {
			soapNamespace = SOAP_11_NAMESPACE;
			soapContentType = SOAP_11_CONTENT_TYPE;
		}

		// soap-action (optional)

		var soapActionValue = configuration.getString(SOAP_ACTION, "").trim();

		if (ValidationUtils.isNotBlank(soapActionValue)) {
			soapAction = soapActionValue;
		}

		// clientBuilder

		clientBuilder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSeconds));

		if (tls.isEnabled()) {
			clientBuilder.sslContext(tls.getKeyStoreAndTrustStoreSSLContext());
		}
	}
}
