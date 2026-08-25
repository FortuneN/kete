package io.github.fortunen.kete.destinations.azurewebpubsub;

import java.time.Duration;
import java.util.Arrays;

import com.azure.core.http.HttpClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.AzureUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"connectionString"})
public class AzureWebPubSubDestinationConfig extends DestinationConfig {

	public static final String HUB = "hub";
	public static final String GROUP = "group";
	public static final String ENDPOINT = "endpoint";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String CONNECTION_STRING = "connection-string";

	private String hub;
	private String group;
	private Duration timeout;
	private boolean hasGroup;
	private int timeoutSeconds;
	private HttpClient httpClient;
	private boolean isHubTemplated;
	private String connectionString;
	private boolean isGroupTemplated;
	private String serviceEndpoint;
	private WebPubSubServiceClientBuilder webPubSubClientBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// connection-string (conditional)

		connectionString = configuration.getString(CONNECTION_STRING, "").trim();

		// authentication-type (optional)

		var hasConnectionString = ValidationUtils.isNotBlank(connectionString);

		if (hasAuthenticationType) {
			ValidationUtils.requireTrue(!hasConnectionString || authenticationType.equals("connection-string"),
				"'" + CONNECTION_STRING + "' cannot be set when '" + AUTHENTICATION_TYPE + "' is '" + authenticationType + "'");
		}

		// endpoint (required for managed-identity / default-azure-credential)

		var endpoint = configuration.getString(ENDPOINT, "").trim();

		// hub (required)

		hub = ValidationUtils.requireNonBlank(configuration.getString(HUB, "").trim(), HUB + " is required");

		// group (optional)

		group = configuration.getString(GROUP, "").trim();
		hasGroup = ValidationUtils.isNotBlank(group);

		// timeout-seconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// timeout

		timeout = Duration.ofSeconds(timeoutSeconds);

		// precomputed fields

		isHubTemplated = TemplateUtils.containsTemplate(hub);
		isGroupTemplated = TemplateUtils.containsTemplate(group);

		// Azure HTTP client (with optional TLS)

		httpClient = AzureUtils.createHttpClient(tls, timeout);

		// WebPubSub client builder (destination calls .buildClient())

		webPubSubClientBuilder = new WebPubSubServiceClientBuilder()
			.hub(hub)
			.httpClient(httpClient);

		if (hasAuthenticationType) {
			AzureUtils.configureAuthentication(authenticationType, configuration, "connection-string",
				() -> {
					ValidationUtils.requireNonBlank(connectionString, CONNECTION_STRING + " is required when " + AUTHENTICATION_TYPE + " is 'connection-string'");
					webPubSubClientBuilder.connectionString(connectionString);
				},
				credential -> {
					ValidationUtils.requireNonBlank(endpoint, ENDPOINT + " is required when " + AUTHENTICATION_TYPE + " is '" + authenticationType + "'");
					ValidationUtils.requireValidUrl(endpoint, ENDPOINT + " must be a valid absolute URL");
					webPubSubClientBuilder.endpoint(endpoint).credential(credential);
				}
			);
		} else {
			ValidationUtils.requireNonBlank(connectionString, "'" + CONNECTION_STRING + "' or '" + AUTHENTICATION_TYPE + "' must be set");
			webPubSubClientBuilder.connectionString(connectionString);
		}

		// service endpoint (for connection verification)

		if (hasConnectionString) {
			serviceEndpoint = Arrays.stream(connectionString.split(";")).filter(part -> part.startsWith("Endpoint=")).map(part -> part.substring("Endpoint=".length())).findFirst().orElseThrow(() -> new IllegalStateException("connection-string does not contain an Endpoint"));
		} else {
			serviceEndpoint = endpoint;
		}
	}
}
