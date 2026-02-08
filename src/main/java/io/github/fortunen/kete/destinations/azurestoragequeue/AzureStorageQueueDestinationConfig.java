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
	public static final String SAS_TOKEN = "sas-token";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String API_VERSION = "2024-08-04";
	public static final String ACCOUNT_KEY = "account-key";
	public static final String MESSAGE_TTL = "message-ttl";
	public static final String ACCOUNT_NAME = "account-name";
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

		// connection-string (mutually exclusive with account-name, account-key, sas-token, url)

		var rawConnectionString = configuration.getString(CONNECTION_STRING, "").trim();
		var hasConnectionString = ValidationUtils.isNotBlank(rawConnectionString);

		if (hasConnectionString) {

			var rawAccountName = configuration.getString(ACCOUNT_NAME, "").trim();
			var rawAccountKey = configuration.getString(ACCOUNT_KEY, "").trim();
			var rawSasToken = configuration.getString(SAS_TOKEN, "").trim();
			var rawUrl = configuration.getString(URL, "").trim(); 

			ValidationUtils.requireFalse(ValidationUtils.isNotBlank(rawAccountName), CONNECTION_STRING + " and " + ACCOUNT_NAME + " are mutually exclusive");
			ValidationUtils.requireFalse(ValidationUtils.isNotBlank(rawAccountKey), CONNECTION_STRING + " and " + ACCOUNT_KEY + " are mutually exclusive");
			ValidationUtils.requireFalse(ValidationUtils.isNotBlank(rawSasToken), CONNECTION_STRING + " and " + SAS_TOKEN + " are mutually exclusive");
			ValidationUtils.requireFalse(ValidationUtils.isNotBlank(rawUrl), CONNECTION_STRING + " and " + URL + " are mutually exclusive");

			var info = AzureStorageQueueUtils.parseConnectionString(rawConnectionString);

			accountName = info.accountName();
			accountKey = info.accountKey();
			sasToken = info.sasToken();
			url = info.url();
			useSasAuth = info.useSasAuth();

		} else {

			// authentication — exactly one of account-key or sas-token

			var rawSasToken = configuration.getString(SAS_TOKEN, "").trim();
			var rawAccountKey = configuration.getString(ACCOUNT_KEY, "").trim();

			useSasAuth = ValidationUtils.isNotBlank(rawSasToken);
			var useSharedKeyAuth = ValidationUtils.isNotBlank(rawAccountKey);

			ValidationUtils.requireTrue(useSasAuth || useSharedKeyAuth, "either " + ACCOUNT_KEY + ", " + SAS_TOKEN + ", or " + CONNECTION_STRING + " is required");
			ValidationUtils.requireFalse(useSasAuth && useSharedKeyAuth, ACCOUNT_KEY + " and " + SAS_TOKEN + " are mutually exclusive");

			if (useSasAuth) {
				sasToken = rawSasToken.startsWith("?") ? rawSasToken.substring(1) : rawSasToken;
			} else {
				accountKey = rawAccountKey;
			}

			// account-name (required for shared-key; optional for sas when url is provided)

			accountName = configuration.getString(ACCOUNT_NAME, "").trim();

			if (useSharedKeyAuth) {
				ValidationUtils.requireNonBlank(accountName, ACCOUNT_NAME + " is required");
			}

			// url (optional — defaults to https://{account-name}.queue.core.windows.net)

			var configuredUrl = configuration.getString(URL, "").trim();

			if (ValidationUtils.isNotBlank(configuredUrl)) {

				url = ValidationUtils.requireValidUrl(configuredUrl, URL + " must be a valid absolute URL");

				if (ValidationUtils.isNotBlank(accountName)) {
					ValidationUtils.requireTrue(url.contains(accountName), URL + " must contain the " + ACCOUNT_NAME);
				}

			} else {

				ValidationUtils.requireNonBlank(accountName, URL + " or " + ACCOUNT_NAME + " is required");

				url = "https://" + accountName + ".queue.core.windows.net";
			}
		}

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
