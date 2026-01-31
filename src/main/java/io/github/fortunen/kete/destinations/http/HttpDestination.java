package io.github.fortunen.kete.destinations.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		var clientBuilder = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(config.getTimeoutSeconds()));

		if (config.getSslContext() != null) {
			clientBuilder.sslContext(config.getSslContext());
		}

		httpClient = clientBuilder.build();

		// test

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

		if (config.isMessageHeadersEnabled()) {
			requestBuilder
				.header(MESSAGE_HEADER_CONTENT_TYPE, message.contentType())
				.header(MESSAGE_HEADER_EVENT_TYPE, message.eventType())
				.header(MESSAGE_HEADER_EVENT_KIND, message.kind());
		}

		if (ValidationUtils.isNotNull(config.getOauth()) && config.getOauth().isEnabled()) {
			requestBuilder.header(AUTHORIZATION, config.getOauth().getAccessToken().toAuthorizationHeader());
		}

		if (ValidationUtils.isNotNull(config.getHeaders()) && !config.getHeaders().isEmpty()) {
			config.getHeaders().forEach(requestBuilder::header);
		}

		if (config.isMethodIsPost()) {
			requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
		} else {
			requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
		}

		var request = requestBuilder.build();
		var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		ValidationUtils.requireTrue(response.statusCode() >= 200 && response.statusCode() < 300, () -> new IOException("HTTP " + response.statusCode() + " : " + response.body()));
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(httpClient, "httpClient");
	}
}
