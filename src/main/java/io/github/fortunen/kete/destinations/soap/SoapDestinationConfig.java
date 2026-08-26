package io.github.fortunen.kete.destinations.soap;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.HttpEndpointUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.github.fortunen.kete.OAuthMaterial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"authHeaderValue", "filteredCustomHeaders"})
public class SoapDestinationConfig extends DestinationConfig {

	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
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

		// endpoint (url wins over host/port/path-and-query/tls.enabled; an https url turns TLS on)

		var endpoint = HttpEndpointUtils.resolveEndpoint(configuration, tls);

		tls = endpoint.tls();
		url = endpoint.url();
		host = endpoint.host();
		port = endpoint.port();
		path = endpoint.path();
		query = endpoint.query();
		scheme = endpoint.scheme();
		hasHost = endpoint.hasHost();
		hasPort = endpoint.hasPort();
		hasPath = endpoint.hasPath();
		parsedUri = endpoint.parsedUri();
		tlsConfig = endpoint.tlsConfig();
		tlsFromUrl = endpoint.tlsFromUrl();
		pathAndQuery = endpoint.pathAndQuery();
		urlFromConfig = endpoint.urlFromConfig();
		hasTlsEnabled = endpoint.hasTlsEnabled();

		// timeoutSeconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// authentication-type

		var authentication = HttpEndpointUtils.resolveAuthentication(configuration, hasAuthenticationType ? authenticationType : null, keycloakRealm, keycloakSession);

		oauth = authentication.oauth();
		oauthEnabled = authentication.oauthEnabled();
		authHeaderName = authentication.headerName();
		authHeaderValue = authentication.headerValue();

		// precomputed fields

		isUrlTemplated = TemplateUtils.containsTemplate(url);
		timeout = Duration.ofSeconds(timeoutSeconds);

		filteredCustomHeaders = HttpEndpointUtils.filterCustomHeaders(customHeadersEntrySet);

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

		clientBuilder = HttpEndpointUtils.createClientBuilder(timeoutSeconds, tls);
	}
}
