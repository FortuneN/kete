package io.github.fortunen.kete.destinations.azurestoragequeue;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;

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
@Component(name = "azure-storage-queue")
public class AzureStorageQueueDestination extends Destination<AzureStorageQueueDestinationConfig> {

	private String queue;
	private Duration timeout;
	private Duration messageTtl;
	private QueueClient queueClient;
	private boolean isQueueTemplated;
	private QueueClientBuilder queueClientBuilder;
	private final ConcurrentHashMap<String, QueueClient> queueClientCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		queue = config.getQueue();
		timeout = config.getTimeout();
		messageTtl = config.getMessageTtlDuration();
		isQueueTemplated = config.isQueueTemplated();
		queueClientBuilder = config.getQueueClientBuilder();

		if (!isQueueTemplated) {
			queueClient = queueClientBuilder.queueName(queue).buildClient();
		}

		// verify connection

		var testHttpClientBuilder = HttpClient.newBuilder();

		if (config.getTls().isEnabled()) {
			testHttpClientBuilder.sslContext(config.getTls().getKeyStoreAndTrustStoreSSLContext());
		}

		var testRequest = HttpRequest.newBuilder().uri(URI.create(config.getQueueServiceEndpoint())).timeout(timeout).GET().build();

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

		// resolve queue client (use cached client for templated queues)

		var actualQueue = isQueueTemplated ? TemplateUtils.substitute(queue, message) : queue;
		var client = isQueueTemplated ? queueClientCache.computeIfAbsent(actualQueue, name -> queueClientBuilder.queueName(name).buildClient()) : queueClient;

		// send message (with TTL if configured)

		var payload = encodePayload(message.eventBody());

		var body = new String(payload, StandardCharsets.UTF_8);

		if (ValidationUtils.isNotNull(messageTtl)) {
			client.sendMessageWithResponse(body, null, messageTtl, timeout, Context.NONE);
		} else {
			client.sendMessage(body);
		}
	}

	@Override
	public void close() {
		queueClientCache.clear();
		// QueueClient does not implement Closeable — no cleanup needed
	}
}
