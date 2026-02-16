package io.github.fortunen.kete.destinations.gcpcloudtasks;

import java.time.Duration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.tasks.v2.HttpMethod;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.GcpUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.grpc.okhttp.OkHttpChannelBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class GcpCloudTasksDestinationConfig extends DestinationConfig {

	public static final String QUEUE = "queue";
	public static final String PROJECT = "project";
	public static final String ENDPOINT = "endpoint";
	public static final String LOCATION = "location";
	public static final String TARGET_URL = "target-url";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String HTTP_METHOD = "http-method";
	public static final String DEFAULT_HTTP_METHOD = "POST";
	public static final String USE_PLAINTEXT = "use-plaintext";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String DEFAULT_ENDPOINT = "cloudtasks.googleapis.com:443";
	public static final String CLOUD_TASKS_SCOPE = "https://www.googleapis.com/auth/cloud-tasks";

	private String queue;
	private String project;
	private String location;
	private String endpoint;
	private Duration timeout;
	private String targetUrl;
	private String httpMethod;
	private int timeoutSeconds;
	private boolean usePlaintext;
	private boolean authenticated;
	private String parentPathPrefix;
	private boolean isQueueTemplated;
	private GoogleCredentials credentials;
	private OkHttpChannelBuilder channelBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// project

		project = ValidationUtils.requireNonBlank(configuration.getString(PROJECT, "").trim(), PROJECT + " is required");

		// location

		location = ValidationUtils.requireNonBlank(configuration.getString(LOCATION, "").trim(), LOCATION + " is required");

		// queue

		queue = ValidationUtils.requireNonBlank(configuration.getString(QUEUE, "").trim(), QUEUE + " is required");

		// target-url

		targetUrl = ValidationUtils.requireValidUrl(configuration.getString(TARGET_URL, "").trim(), TARGET_URL + " is required and must be a valid absolute URL");

		// http-method

		httpMethod = configuration.getString(HTTP_METHOD, DEFAULT_HTTP_METHOD).trim().toUpperCase();

		try {
			HttpMethod.valueOf(httpMethod);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(HTTP_METHOD + " must be a valid HTTP method (POST, GET, PUT, DELETE, PATCH, HEAD, OPTIONS)");
		}

		// endpoint (gRPC endpoint, e.g. "cloudtasks.googleapis.com:443" or "localhost:8123")

		endpoint = configuration.getString(ENDPOINT, DEFAULT_ENDPOINT).trim();

		// use-plaintext (for emulator — no TLS on gRPC channel)

		usePlaintext = configuration.getBoolean(USE_PLAINTEXT, false);

		// credentials

		credentials = GcpUtils.configureAuthentication(authenticationType, configuration, CLOUD_TASKS_SCOPE);
		authenticated = ValidationUtils.isNotNull(credentials);

		ValidationUtils.requireFalse(!authenticated && DEFAULT_ENDPOINT.equals(endpoint), "credentials are required for " + DEFAULT_ENDPOINT);

		// timeoutSeconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// timeout

		timeout = Duration.ofSeconds(timeoutSeconds);

		// precomputed fields

		isQueueTemplated = TemplateUtils.containsTemplate(queue);
		parentPathPrefix = "projects/" + project + "/locations/" + location + "/queues/";

		// client builder

		channelBuilder = OkHttpChannelBuilder.forTarget(endpoint);

		if (usePlaintext) {
			channelBuilder.usePlaintext();
		} else if (tls.isEnabled()) {
			channelBuilder.sslSocketFactory(tls.getKeyStoreAndTrustStoreSSLContext().getSocketFactory());
		}
	}
}
