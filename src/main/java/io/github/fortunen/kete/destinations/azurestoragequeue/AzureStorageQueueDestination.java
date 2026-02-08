package io.github.fortunen.kete.destinations.azurestoragequeue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.spec.SecretKeySpec;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.AzureStorageQueueUtils;
import io.github.fortunen.kete.utils.Base64Utils;
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

	private static final String HEADER_X_MS_DATE = "x-ms-date";
	private static final String APPLICATION_XML = "application/xml";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_X_MS_VERSION = "x-ms-version";
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final DateTimeFormatter RFC_1123_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

	private Duration timeout;
	private String apiVersion;
	private boolean useSasAuth;
	private String querySuffix;
	private HttpClient httpClient;
	private String messagesUrlPrefix;
	private String authorizationPrefix;
	private SecretKeySpec secretKeySpec;
	private String canonicalResourcePrefix;
	private String messageTtlCanonicalSuffix;
	private record QueueContext(URI requestUri, String canonicalResource) {}
	private final ConcurrentHashMap<String, QueueContext> queueContextCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		var info = config.getConnectionStringInfo();
		var url = info.url();

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		messagesUrlPrefix = url + "/";
		timeout = config.getTimeout();
		useSasAuth = info.useSasAuth();
		httpClient = config.getClientBuilder().build();

		// query suffix (combines message-ttl and sas-token)

		var query = new StringBuilder();

		if (config.getMessageTtl() != 0) {
			query.append("messagettl=").append(config.getMessageTtl());
		}

		if (useSasAuth) {

			if (!query.isEmpty()) {
				query.append('&');
			}

			query.append(info.sasToken());
		}

		querySuffix = query.isEmpty() ? "" : "?" + query;

		// shared-key auth precomputation

		if (!useSasAuth) {
			apiVersion = AzureStorageQueueDestinationConfig.API_VERSION;
			authorizationPrefix = "SharedKey " + info.accountName() + ":";
			canonicalResourcePrefix = "/" + info.accountName();
			messageTtlCanonicalSuffix = config.getMessageTtl() != 0 ? "\nmessagettl:" + config.getMessageTtl() : "";
			secretKeySpec = AzureStorageQueueUtils.buildSecretKeySpec(info.accountKey());
		}

		// verify connection

		var testRequest = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.timeout(timeout)
			.GET()
			.build();

		httpClient.send(testRequest, HttpResponse.BodyHandlers.discarding());
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// queue context (request URI + canonical resource, cached per queue name)

		var actualQueue = TemplateUtils.substitute(config.getQueue(), message);

		var ctx = queueContextCache.computeIfAbsent(actualQueue, queue -> {
			var base = messagesUrlPrefix + queue + "/messages";
			var uri = URI.create(querySuffix.isEmpty() ? base : base + querySuffix);
			var canonical = useSasAuth ? null : canonicalResourcePrefix + uri.getPath() + messageTtlCanonicalSuffix;
			return new QueueContext(uri, canonical);
		});

		// body — wrap event in Azure Queue XML envelope with base64-encoded content

		var base64Data = Base64Utils.encode(message.eventBody());
		var bodyBytes = ("<QueueMessage><MessageText>" + base64Data + "</MessageText></QueueMessage>").getBytes(StandardCharsets.UTF_8);

		// request

		var requestBuilder = HttpRequest.newBuilder()
			.timeout(timeout)
			.uri(ctx.requestUri())
			.header(HEADER_CONTENT_TYPE, APPLICATION_XML)
			.POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes));

		// shared-key authorization

		if (!useSasAuth) {

			var date = ZonedDateTime.now(ZoneOffset.UTC).format(RFC_1123_FORMATTER);
			var stringToSign = AzureStorageQueueUtils.buildStringToSign(bodyBytes.length, date, apiVersion, ctx.canonicalResource());
			var signature = AzureStorageQueueUtils.computeSignature(secretKeySpec, stringToSign);

			requestBuilder
				.header(HEADER_X_MS_DATE, date)
				.header(HEADER_X_MS_VERSION, apiVersion)
				.header(HEADER_AUTHORIZATION, authorizationPrefix + signature);
		}

		// request

		var response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

		ValidationUtils.requireTrue(response.statusCode() >= 200 && response.statusCode() < 300, () -> new IOException("Azure Storage Queue put message failed: HTTP " + response.statusCode() + " : " + response.body()));
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(httpClient, "httpClient");
	}
}
