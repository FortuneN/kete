package io.github.fortunen.kete.destinations.gcppubsub;

import java.time.Duration;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.GcpUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
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
	public static final String PUBSUB_SCOPE = "https://www.googleapis.com/auth/pubsub";

	private String url;
	private String topic;
	private String project;
	private Duration timeout;
	private String orderingKey;
	private int timeoutSeconds;
	private boolean hasTimeoutSeconds;
	private boolean authenticated;
	private boolean hasOrderingKey;
	private boolean isTopicTemplated;
	private String publishTopicPrefix;
	private HttpTransport httpTransport;
	private GoogleCredentials credentials;
	private boolean isOrderingKeyTemplated;

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

		// credentials

		credentials = GcpUtils.configureAuthentication(authenticationType, configuration, PUBSUB_SCOPE);
		authenticated = ValidationUtils.isNotNull(credentials);

		ValidationUtils.requireFalse(!authenticated && DEFAULT_URL.equalsIgnoreCase(url), "credentials are required for " + DEFAULT_URL);

		// ordering key (optional)

		orderingKey = configuration.getString(ORDERING_KEY, "").trim();

		// timeoutSeconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// timeout

		timeout = Duration.ofSeconds(timeoutSeconds);
		hasTimeoutSeconds = configuration.containsKey(TIMEOUT_SECONDS);

		// precomputed fields

		isTopicTemplated = TemplateUtils.containsTemplate(topic);
		hasOrderingKey = ValidationUtils.isNotBlank(orderingKey);
		isOrderingKeyTemplated = TemplateUtils.containsTemplate(orderingKey);
		publishTopicPrefix = "projects/" + project + "/topics/";

		// HTTP transport (with optional TLS)

		if (tls.isEnabled()) {
			httpTransport = new NetHttpTransport.Builder().setSslSocketFactory(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory()).build();
		} else {
			httpTransport = new NetHttpTransport();
		}
	}
}
