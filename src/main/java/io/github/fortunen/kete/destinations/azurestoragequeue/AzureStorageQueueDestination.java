package io.github.fortunen.kete.destinations.azurestoragequeue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final String APPLICATION_XML = "application/xml";
	private static final DateTimeFormatter RFC_1123_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

	private HttpClient httpClient;
	private SecretKeySpec secretKeySpec;
	private String messagesUrlPrefix;
	private String messageTtlQuerySuffix;
	private String messageTtlCanonicalSuffix;
	private String accountResourcePrefix;
	private String authorizationPrefix;
	private String apiVersion;
	private final ConcurrentHashMap<String, URI> messagesUrlCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		httpClient = config.getClientBuilder().build();

		messagesUrlPrefix = config.getUrl() + "/";

		secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(config.getAccountKey()), HMAC_SHA256);

		apiVersion = AzureStorageQueueDestinationConfig.API_VERSION;

		messageTtlQuerySuffix = config.getMessageTtl() != 0 ? "?messagettl=" + config.getMessageTtl() : "";

		messageTtlCanonicalSuffix = config.getMessageTtl() != 0 ? "\nmessagettl:" + config.getMessageTtl() : "";

		accountResourcePrefix = "/" + config.getAccountName() + "/";

		authorizationPrefix = "SharedKey " + config.getAccountName() + ":";

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

		// queue name (supports templating)

		var actualQueue = TemplateUtils.substitute(config.getQueue(), message);

		// messages url

		var messagesUri = messagesUrlCache.computeIfAbsent(actualQueue, queue -> URI.create(messagesUrlPrefix + queue + "/messages"));

		// body — wrap event in Azure Queue XML envelope with base64-encoded content

		var base64Data = Base64.getEncoder().encodeToString(message.eventBody());
		var xmlBody = "<QueueMessage><MessageText>" + base64Data + "</MessageText></QueueMessage>";
		var bodyBytes = xmlBody.getBytes(StandardCharsets.UTF_8);

		// request uri

		var requestUri = messageTtlQuerySuffix.isEmpty() ? messagesUri : URI.create(messagesUri + messageTtlQuerySuffix);

		// authorization

		var date = ZonedDateTime.now(ZoneOffset.UTC).format(RFC_1123_FORMATTER);

		var stringToSign = "POST\n\n\n"  // verb, content-encoding, content-language
			+ bodyBytes.length + "\n"    // content-length
			+ "\n"                       // content-md5
			+ APPLICATION_XML + "\n"     // content-type
			+ "\n\n\n\n\n"              // date, if-modified-since, if-match, if-none-match, if-unmodified-since, range
			+ "x-ms-date:" + date + "\n"
			+ "x-ms-version:" + apiVersion + "\n"
			+ accountResourcePrefix + actualQueue + "/messages" + messageTtlCanonicalSuffix;

		var mac = Mac.getInstance(HMAC_SHA256);
		mac.init(secretKeySpec);
		var signature = Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));

		// request

		var request = HttpRequest.newBuilder()
			.uri(requestUri)
			.timeout(config.getTimeout())
			.header("Content-Type", APPLICATION_XML)
			.header("x-ms-date", date)
			.header("x-ms-version", apiVersion)
			.header("Authorization", authorizationPrefix + signature)
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
