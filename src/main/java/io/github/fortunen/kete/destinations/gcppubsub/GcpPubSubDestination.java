package io.github.fortunen.kete.destinations.gcppubsub;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.Base64Utils;
import io.github.fortunen.kete.utils.JsonUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Component(name = "gcp-pubsub")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class GcpPubSubDestination extends Destination<GcpPubSubDestinationConfig> {

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String AUTHORIZATION = "Authorization";
	private static final String APPLICATION_JSON = "application/json";

	private HttpClient httpClient;
	private String publishUrlPrefix;
	private final ConcurrentHashMap<String, URI> publishUrlCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		httpClient = config.getClientBuilder().build();

		publishUrlPrefix = config.getUrl() + "/v1/projects/" + config.getProject() + "/topics/";

		// verify connection

		var testRequest = HttpRequest.newBuilder()
			.uri(URI.create(config.getUrl()))
			.timeout(config.getTimeout())
			.GET()
			.build();

		httpClient.send(testRequest, HttpResponse.BodyHandlers.discarding());
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// topic

		var actualTopic = TemplateUtils.substitute(config.getTopic(), message);

		// publish url

		var publishUri = publishUrlCache.computeIfAbsent(actualTopic, topic -> URI.create(publishUrlPrefix + topic + ":publish"));

		// body

		var rootObject = JsonUtils.createObjectNode();
		var messagesArray = rootObject.putArray("messages");
		var messageObject = messagesArray.addObject();

        if (ValidationUtils.isNotBlank(config.getOrderingKey())) {
			messageObject.put("orderingKey", config.getOrderingKey());
		}

		messageObject.put("data", Base64Utils.encode(message.eventBody()));

		var attributes = messageObject.putObject("attributes");

		attributes.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		attributes.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		attributes.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		var bodyJson = rootObject.toString();

		// request

		var requestBuilder = HttpRequest.newBuilder()
			.uri(publishUri)
			.timeout(config.getTimeout())
			.header(CONTENT_TYPE, APPLICATION_JSON);

		if (config.isAuthenticated()) {
			requestBuilder.header(AUTHORIZATION, config.getAuth().getAccessToken().toAuthorizationHeader());
		}

		requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyJson));

		var response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

		ValidationUtils.requireTrue(response.statusCode() >= 200 && response.statusCode() < 300, () -> new IOException("GCP Pub/Sub publish failed: HTTP " + response.statusCode() + " : " + response.body()));
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(httpClient, "httpClient");
	}
}
