package io.github.fortunen.kete.destinations.azurewebpubsub;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "azure-webpubsub")
public class AzureWebPubSubDestination extends Destination<AzureWebPubSubDestinationConfig> {

	private String hub;
	private String group;
	private boolean hasGroup;
	private boolean isHubTemplated;
	private boolean isGroupTemplated;
	private Duration timeout;
	private WebPubSubServiceClient webPubSubClient;
	private WebPubSubServiceClientBuilder webPubSubClientBuilder;
	private final ConcurrentHashMap<String, WebPubSubServiceClient> hubClientCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		hub = config.getHub();
		group = config.getGroup();
		timeout = config.getTimeout();
		hasGroup = config.isHasGroup();
		isHubTemplated = config.isHubTemplated();
		isGroupTemplated = config.isGroupTemplated();
		webPubSubClientBuilder = config.getWebPubSubClientBuilder();

		if (!isHubTemplated) {
			webPubSubClient = webPubSubClientBuilder.buildClient();
		}

		// verify connection

		var endpointUrl = config.getServiceEndpoint();

		var testHttpClientBuilder = HttpClient.newBuilder();

		if (config.getTls().isEnabled()) {
			testHttpClientBuilder.sslContext(config.getTls().getKeyStoreAndTrustStoreSSLContext());
		}

		var testRequest = HttpRequest.newBuilder().uri(URI.create(endpointUrl)).timeout(timeout).GET().build();

		testHttpClientBuilder.build().send(testRequest, HttpResponse.BodyHandlers.discarding());
	}

	@Override
	public boolean isHealthy() {
		return true; // stateless HTTP client; nothing to probe
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var payload = encodePayload(message.eventBody());

		var body = new String(payload);
		var contentType = message.contentType().contains("json") ? WebPubSubContentType.APPLICATION_JSON : WebPubSubContentType.TEXT_PLAIN;

		// resolve hub (use cached client for templated hubs)

		var actualHub = isHubTemplated ? TemplateUtils.substitute(hub, message) : hub;
		var client = isHubTemplated ? hubClientCache.computeIfAbsent(actualHub, h -> webPubSubClientBuilder.hub(h).buildClient()) : webPubSubClient;

		// resolve group

		var actualGroup = isGroupTemplated ? TemplateUtils.substitute(group, message) : group;

		if (hasGroup || (isGroupTemplated && ValidationUtils.isNotBlank(actualGroup))) {
			client.sendToGroup(actualGroup, body, contentType);
		} else {
			client.sendToAll(body, contentType);
		}
	}

	@Override
	public void close() {
		hubClientCache.clear();
		// WebPubSubServiceClient is stateless — no cleanup needed
	}
}
