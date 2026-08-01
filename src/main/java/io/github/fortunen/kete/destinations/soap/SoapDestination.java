package io.github.fortunen.kete.destinations.soap;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.OAuthMaterial;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "soap")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class SoapDestination extends Destination<SoapDestinationConfig> {

	public static final String AUTHORIZATION = "Authorization";
	public static final String MESSAGE_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String MESSAGE_HEADER_EVENT_TYPE = "x-" + Constants.MESSAGE_HEADER_EVENT_TYPE;
	public static final String MESSAGE_HEADER_EVENT_KIND = "x-" + Constants.MESSAGE_HEADER_EVENT_KIND;

	private String url;
	private Duration timeout;
	private OAuthMaterial oauth;
	private HttpClient httpClient;
	private boolean isUrlTemplated;
	private boolean isOauthEnabled;
	private String authHeaderName;
	private String authHeaderValue;
	private String soapAction;
	private String soapNamespace;
	private String soapContentType;
	private Set<Map.Entry<String, String>> customHeaders;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		url = config.getUrl();
		oauth = config.getOauth();
		timeout = config.getTimeout();
		isUrlTemplated = config.isUrlTemplated();
		isOauthEnabled = config.isOauthEnabled();
		authHeaderName = config.getAuthHeaderName();
		authHeaderValue = config.getAuthHeaderValue();
		httpClient = config.getClientBuilder().build();
		customHeaders = config.getFilteredCustomHeaders();
		soapAction = config.getSoapAction();
		soapNamespace = config.getSoapNamespace();
		soapContentType = config.getSoapContentType();

		// verify connection

		var testRequest = HttpRequest.newBuilder()
			.uri(URI.create(config.getScheme() + "://" + config.getHost() + ":" + config.getPort()))
			.timeout(timeout)
			.GET()
			.build();

		httpClient.send(testRequest, HttpResponse.BodyHandlers.discarding());
	}

	@Override
	public boolean isHealthy() {
		return true; // stateless HTTP client; nothing to probe
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// actualUrl

		var actualUrl = isUrlTemplated ? TemplateUtils.substitute(url, message) : url;

		// soap envelope

		var payloadString = new String(message.eventBody(), StandardCharsets.UTF_8);

		var envelope = "<soap:Envelope xmlns:soap=\"" + soapNamespace + "\">"
			+ "<soap:Body>"
			+ payloadString
			+ "</soap:Body>"
			+ "</soap:Envelope>";

		var payload = encodePayload(envelope.getBytes(StandardCharsets.UTF_8));

		var body = new String(payload, StandardCharsets.UTF_8);

		// request

		var requestBuilder = HttpRequest
			.newBuilder()
			.uri(URI.create(actualUrl))
			.timeout(timeout);

		if (isOauthEnabled) {
			requestBuilder.header(AUTHORIZATION, oauth.getAccessToken().toAuthorizationHeader());
		} else if (ValidationUtils.isNotNull(authHeaderName)) {
			requestBuilder.header(authHeaderName, authHeaderValue);
		}

		// custom headers

		for (var entry : customHeaders) {
			requestBuilder.header(entry.getKey(), entry.getValue());
		}

		// standard headers

		requestBuilder
			.header(MESSAGE_HEADER_EVENT_KIND, message.kind())
			.header(MESSAGE_HEADER_EVENT_TYPE, message.eventType())
			.header(MESSAGE_HEADER_CONTENT_TYPE, soapContentType);

		// soap action header

		if (soapAction != null) {
			requestBuilder.header("SOAPAction", soapAction);
		}

		// encoding headers

		if (config.getContentEncodingName() != null) {
			requestBuilder.header("Content-Encoding", config.getContentEncodingName());
		}

		if (config.getContentTransferEncodingName() != null) {
			requestBuilder.header("Content-Transfer-Encoding", config.getContentTransferEncodingName());
		}

		requestBuilder.method("POST", HttpRequest.BodyPublishers.ofString(body));

		var response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

		ValidationUtils.requireTrue(response.statusCode() >= 200 && response.statusCode() < 300, () -> new IOException("HTTP " + response.statusCode() + " : " + response.body()));
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(httpClient, "httpClient");
	}
}
