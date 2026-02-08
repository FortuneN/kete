package io.github.fortunen.kete.destinations.gcppubsub;

import java.net.http.HttpClient;
import java.time.Duration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.GcpAuthMaterial;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class GcpPubSubDestinationConfig extends DestinationConfig {

	public static final String URL = "url";
	public static final String TOPIC = "topic";
	public static final String PROJECT = "project";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String ORDERING_KEY = "ordering-key";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String DEFAULT_URL = "https://pubsub.googleapis.com";
	public static final String CREDENTIALS_FILE_PATH = "credentials-file-path";
	public static final String CREDENTIALS_FILE_TEXT = "credentials-file-text";
	public static final String CREDENTIALS_FILE_BASE64 = "credentials-file-base64";

	private String url;
	private String topic;
	private String project;
	private String orderingKey;
	private int timeoutSeconds;
	private Duration timeout;
	private boolean authenticated;
	private GcpAuthMaterial auth;
	private HttpClient.Builder clientBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// project

		project = ValidationUtils.requireNonBlank(configuration.getString(PROJECT, "").trim(), PROJECT + " is required");

		// topic

		topic = ValidationUtils.requireNonBlank(configuration.getString(TOPIC, "").trim(), TOPIC + " is required");

		// url

		url = ValidationUtils.requireValidUrl(configuration.getString(URL, DEFAULT_URL).trim(), URL + " must be a valid absolute URL");

		// credentials (optional — omit for emulator / no-auth mode, enforce at most one)

		var credentialsFilePath = configuration.getString(CREDENTIALS_FILE_PATH, "").trim();
		var credentialsFileText = configuration.getString(CREDENTIALS_FILE_TEXT, "").trim();
		var credentialsFileBase64 = configuration.getString(CREDENTIALS_FILE_BASE64, "").trim();

		var credentialsSources = (ValidationUtils.isNotBlank(credentialsFilePath) ? 1 : 0) + (ValidationUtils.isNotBlank(credentialsFileText) ? 1 : 0) + (ValidationUtils.isNotBlank(credentialsFileBase64) ? 1 : 0);

		ValidationUtils.requireFalse(credentialsSources > 1, CREDENTIALS_FILE_PATH + ", " + CREDENTIALS_FILE_TEXT + ", and " + CREDENTIALS_FILE_BASE64 + " are mutually exclusive");

		if (ValidationUtils.isNotBlank(credentialsFilePath)) {
			auth = GcpAuthMaterial.fromCredentialsFilePath(credentialsFilePath);
		} else if (ValidationUtils.isNotBlank(credentialsFileText)) {
			auth = GcpAuthMaterial.fromCredentialsText(credentialsFileText);
		} else if (ValidationUtils.isNotBlank(credentialsFileBase64)) {
			auth = GcpAuthMaterial.fromCredentialsBase64(credentialsFileBase64);
		}

		authenticated = ValidationUtils.isNotNull(auth);

		ValidationUtils.requireFalse(!authenticated && DEFAULT_URL.equalsIgnoreCase(url), "credentials are required for " + DEFAULT_URL);

		// ordering key (optional)

		orderingKey = configuration.getString(ORDERING_KEY, "").trim();

		// timeoutSeconds

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
