package io.github.fortunen.kete.destinations.azurestoragequeue;

import java.net.http.HttpClient;
import java.time.Duration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.AzureStorageQueueUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AzureStorageQueueDestinationConfig extends DestinationConfig {

	public static final String QUEUE = "queue";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String API_VERSION = "2024-08-04";
	public static final String MESSAGE_TTL = "message-ttl";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String CONNECTION_STRING = "connection-string";

	private String url;
	private String queue;
	private int messageTtl;
	private String sasToken;
	private Duration timeout;
	private String accountKey;
	private String accountName;
	private boolean useSasAuth;
	private int timeoutSeconds;
	private HttpClient.Builder clientBuilder;

	@Override
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// connection-string (required)

		var rawConnectionString = ValidationUtils.requireNonBlank(configuration.getString(CONNECTION_STRING, "").trim(), CONNECTION_STRING + " is required");

		var info = AzureStorageQueueUtils.parseConnectionString(rawConnectionString);

		accountName = info.accountName();
		accountKey = info.accountKey();
		sasToken = info.sasToken();
		url = info.url();
		useSasAuth = info.useSasAuth();

		// strip trailing slash

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		// queue

		queue = ValidationUtils.requireNonBlank(configuration.getString(QUEUE, "").trim(), QUEUE + " is required");

		// message-ttl (optional — -1 means no expiry, 0 means default 7 days)

		messageTtl = configuration.getInt(MESSAGE_TTL, 0);

		ValidationUtils.requireTrue(messageTtl >= -1, MESSAGE_TTL + " must be -1 or greater");

		// timeout-seconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// timeout

		timeout = Duration.ofSeconds(timeoutSeconds);

		// clientBuilder

		clientBuilder = HttpClient.newBuilder().connectTimeout(timeout);

		if (tls.isEnabled()) {
			clientBuilder.sslContext(tls.getKeyStoreAndTrustStoreSSLContext());
		}
	}
}
