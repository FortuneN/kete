package io.github.fortunen.kete.destinations.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "http")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class HttpDestination extends Destination<HttpDestinationConfig> {

	public static final String AUTHORIZATION = "Authorization";
	public static final String MESSAGE_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String MESSAGE_HEADER_EVENT_TYPE = "x-" + Constants.MESSAGE_HEADER_EVENT_TYPE;
	public static final String MESSAGE_HEADER_EVENT_KIND = "x-" + Constants.MESSAGE_HEADER_EVENT_KIND;

	private HttpClient httpClient;
	private Set<Map.Entry<String, String>> customHeaders;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		// customHeaders

		customHeaders = config.getCustomHeadersEntrySet().stream()
			.filter(entry -> {
				var key = entry.getKey();
				return !MESSAGE_HEADER_CONTENT_TYPE.equalsIgnoreCase(key) && !MESSAGE_HEADER_EVENT_KIND.equalsIgnoreCase(key) && !MESSAGE_HEADER_EVENT_TYPE.equalsIgnoreCase(key);
			})
			.collect(Collectors.toSet());

		httpClient = config.getClientBuilder().build();

		// verify connection

		var testRequest = HttpRequest.newBuilder()
			.uri(URI.create(config.getScheme() + "://" + config.getHost() + ":" + config.getPort()))
			.timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
			.GET()
			.build();

		httpClient.send(testRequest, HttpResponse.BodyHandlers.discarding());
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// actualUrl

		var actualUrl = TemplateUtils.substitute(config.getUrl(), message);

		// body

		var body = new String(message.eventBody(), StandardCharsets.UTF_8);

		// request

		var requestBuilder = HttpRequest
			.newBuilder()
			.uri(URI.create(actualUrl))
			.timeout(Duration.ofSeconds(config.getTimeoutSeconds()));

		if (config.isOauthEnabled()) {
			requestBuilder.header(AUTHORIZATION, config.getOauth().getAccessToken().toAuthorizationHeader());
		}

		// headers

		for (var entry : customHeaders) {
			requestBuilder.header(entry.getKey(), entry.getValue());
		}

		requestBuilder
			.header(MESSAGE_HEADER_EVENT_KIND, message.kind())
			.header(MESSAGE_HEADER_EVENT_TYPE, message.eventType())
			.header(MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		if (config.isMethodIsPost()) {
			requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
		} else {
			requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
		}

		var response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

		ValidationUtils.requireTrue(response.statusCode() >= 200 && response.statusCode() < 300, () -> new IOException("HTTP " + response.statusCode() + " : " + response.body()));
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(httpClient, "httpClient");
	}
}
