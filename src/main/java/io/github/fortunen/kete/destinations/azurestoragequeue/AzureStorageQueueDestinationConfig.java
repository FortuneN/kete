package io.github.fortunen.kete.destinations.azurestoragequeue;

import java.net.http.HttpClient;
import java.time.Duration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AzureStorageQueueDestinationConfig extends DestinationConfig {

	public static final String URL = "url";
	public static final String QUEUE = "queue";
	public static final String ACCOUNT_NAME = "account-name";
	public static final String ACCOUNT_KEY = "account-key";
	public static final String MESSAGE_TTL = "message-ttl";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String API_VERSION = "2024-08-04";

	private String url;
	private String queue;
	private String accountName;
	private String accountKey;
	private int messageTtl;
	private int timeoutSeconds;
	private Duration timeout;
	private HttpClient.Builder clientBuilder;

	@Override
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// account-name

		accountName = ValidationUtils.requireNonBlank(configuration.getString(ACCOUNT_NAME, "").trim(), ACCOUNT_NAME + " is required");

		// account-key

		accountKey = ValidationUtils.requireNonBlank(configuration.getString(ACCOUNT_KEY, "").trim(), ACCOUNT_KEY + " is required");

		// queue

		queue = ValidationUtils.requireNonBlank(configuration.getString(QUEUE, "").trim(), QUEUE + " is required");

		// url (optional — defaults to https://{account-name}.queue.core.windows.net)

		var configuredUrl = configuration.getString(URL, "").trim();

		if (ValidationUtils.isNotBlank(configuredUrl)) {
			url = ValidationUtils.requireValidUrl(configuredUrl, URL + " must be a valid absolute URL");
		} else {
			url = "https://" + accountName + ".queue.core.windows.net";
		}

		// strip trailing slash

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

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
