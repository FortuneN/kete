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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
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

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final String SIGNATURE_VERB_PART = "POST\n\n\n";
	private static final String APPLICATION_XML = "application/xml";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_X_MS_DATE = "x-ms-date";
	private static final String HEADER_X_MS_VERSION = "x-ms-version";
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final DateTimeFormatter RFC_1123_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
	private static final String SIGNATURE_CONTENT_TYPE_PART = "\n\n" + APPLICATION_XML + "\n\n\n\n\n\n";

	private Duration timeout;
	private String apiVersion;
	private HttpClient httpClient;
	private String xMsVersionLine;
	private String messagesUrlPrefix;
	private String authorizationPrefix;
	private String messageTtlQuerySuffix;
	private SecretKeySpec secretKeySpec;
	private String accountResourcePrefix;
	private String messageTtlCanonicalSuffix;
	private record QueueContext(URI requestUri, String canonicalResource) {}
	private final ConcurrentHashMap<String, QueueContext> queueContextCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		httpClient = config.getClientBuilder().build();

		timeout = config.getTimeout();

		apiVersion = AzureStorageQueueDestinationConfig.API_VERSION;

		xMsVersionLine = "x-ms-version:" + apiVersion + "\n";

		authorizationPrefix = "SharedKey " + config.getAccountName() + ":";

		messagesUrlPrefix = config.getUrl() + "/";

		messageTtlQuerySuffix = config.getMessageTtl() != 0 ? "?messagettl=" + config.getMessageTtl() : "";

		messageTtlCanonicalSuffix = config.getMessageTtl() != 0 ? "\nmessagettl:" + config.getMessageTtl() : "";

		accountResourcePrefix = "/" + config.getAccountName() + "/";

		secretKeySpec = new SecretKeySpec(Base64Utils.decode(config.getAccountKey()), HMAC_SHA256);

		// verify connection

		var testRequest = HttpRequest.newBuilder()
			.uri(URI.create(config.getUrl()))
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
			var uri = URI.create(messageTtlQuerySuffix.isEmpty() ? base : base + messageTtlQuerySuffix);
			var canonical = accountResourcePrefix + queue + "/messages" + messageTtlCanonicalSuffix;
			return new QueueContext(uri, canonical);
		});

		// body — wrap event in Azure Queue XML envelope with base64-encoded content

		var base64Data = Base64Utils.encode(message.eventBody());
		var xmlBody = "<QueueMessage><MessageText>" + base64Data + "</MessageText></QueueMessage>";
		var bodyBytes = xmlBody.getBytes(StandardCharsets.UTF_8);

		// authorization

		var date = ZonedDateTime.now(ZoneOffset.UTC).format(RFC_1123_FORMATTER);

		var stringToSign = SIGNATURE_VERB_PART
			+ bodyBytes.length               // content-length
			+ SIGNATURE_CONTENT_TYPE_PART     // content-md5, content-type, date, if-modified-since, if-match, if-none-match, if-unmodified-since, range
			+ "x-ms-date:" + date + "\n"
			+ xMsVersionLine
			+ ctx.canonicalResource();

		var mac = Mac.getInstance(HMAC_SHA256);
		mac.init(secretKeySpec);
		var signature = Base64Utils.encode(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));

		// request

		var request = HttpRequest.newBuilder()
			.uri(ctx.requestUri())
			.timeout(timeout)
			.header(HEADER_CONTENT_TYPE, APPLICATION_XML)
			.header(HEADER_X_MS_DATE, date)
			.header(HEADER_X_MS_VERSION, apiVersion)
			.header(HEADER_AUTHORIZATION, authorizationPrefix + signature)
			.POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
			.build();

		var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		ValidationUtils.requireTrue(response.statusCode() >= 200 && response.statusCode() < 300, () -> new IOException("Azure Storage Queue put message failed: HTTP " + response.statusCode() + " : " + response.body()));
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(httpClient, "httpClient");
	}
}
