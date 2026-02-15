package io.github.fortunen.kete.destinations.azurestoragequeue;

import java.time.Duration;
import java.util.Arrays;

import com.azure.core.http.HttpClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.QueueMessageEncoding;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.AzureUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AzureStorageQueueDestinationConfig extends DestinationConfig {

	public static final String QUEUE = "queue";
	public static final String ENDPOINT = "endpoint";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String MESSAGE_TTL = "message-ttl";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String CONNECTION_STRING = "connection-string";

	private String queue;
	private int messageTtl;
	private Duration timeout;
	private int timeoutSeconds;
	private HttpClient httpClient;
	private String connectionString;
	private boolean isQueueTemplated;
	private String queueServiceEndpoint;
	private Duration messageTtlDuration;
	private QueueClientBuilder queueClientBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// connection-string (conditional)

		connectionString = configuration.getString(CONNECTION_STRING, "").trim();

		// authentication-type (optional)

		var hasConnectionString = ValidationUtils.isNotBlank(connectionString);

		if (hasAuthenticationType) {
			ValidationUtils.requireTrue(!hasConnectionString || authenticationType.equals("connection-string"), "'" + CONNECTION_STRING + "' cannot be set when '" + AUTHENTICATION_TYPE + "' is '" + authenticationType + "'");
		}

		// endpoint (required for managed-identity / default-azure-credential)

		var endpoint = configuration.getString(ENDPOINT, "").trim();

		// queue

		queue = ValidationUtils.requireNonBlank(configuration.getString(QUEUE, "").trim(), QUEUE + " is required");

		// message-ttl (optional — -1 means no expiry, 0 means default 7 days)

		messageTtl = configuration.getInt(MESSAGE_TTL, 0);

		ValidationUtils.requireTrue(messageTtl >= -1, MESSAGE_TTL + " must be -1 or greater");

		// timeout-seconds

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");

		// timeout

		timeout = Duration.ofSeconds(timeoutSeconds);

		// precomputed fields

		isQueueTemplated = TemplateUtils.containsTemplate(queue);
		messageTtlDuration = messageTtl == 0 ? null : Duration.ofSeconds(messageTtl);

		// Azure HTTP client (with optional TLS)

		httpClient = AzureUtils.createHttpClient(tls, timeout);

		// Queue client builder (destination calls .queueName(name).buildClient())

		queueClientBuilder = new QueueClientBuilder()
			.messageEncoding(QueueMessageEncoding.BASE64)
			.httpClient(httpClient);

		if (hasAuthenticationType) {
			AzureUtils.configureAuthentication(authenticationType, configuration, "connection-string",
				() -> {
					ValidationUtils.requireNonBlank(connectionString, CONNECTION_STRING + " is required when " + AUTHENTICATION_TYPE + " is 'connection-string'");
					queueClientBuilder.connectionString(connectionString);
				},
				credential -> {
					ValidationUtils.requireNonBlank(endpoint, ENDPOINT + " is required when " + AUTHENTICATION_TYPE + " is '" + authenticationType + "'");
					ValidationUtils.requireValidUrl(endpoint, ENDPOINT + " must be a valid absolute URL");
					queueClientBuilder.endpoint(endpoint).credential(credential);
				}
			);
		} else {
			ValidationUtils.requireNonBlank(connectionString, "'" + CONNECTION_STRING + "' or '" + AUTHENTICATION_TYPE + "' must be set");
			queueClientBuilder.connectionString(connectionString);
		}

		// queue service endpoint (for connection verification)

		if (hasConnectionString) {
			var parts = connectionString.split(";");
			var explicitEndpoint = Arrays.stream(parts).filter(p -> p.startsWith("QueueEndpoint=")).map(p -> p.substring("QueueEndpoint=".length())).findFirst();

			if (explicitEndpoint.isPresent()) {
				queueServiceEndpoint = explicitEndpoint.get();
			} else {
				var protocol = Arrays.stream(parts).filter(p -> p.startsWith("DefaultEndpointsProtocol=")).map(p -> p.substring("DefaultEndpointsProtocol=".length())).findFirst().orElse("https");
				var accountName = Arrays.stream(parts).filter(p -> p.startsWith("AccountName=")).map(p -> p.substring("AccountName=".length())).findFirst().orElseThrow(() -> new IllegalStateException("connection-string must contain AccountName or QueueEndpoint"));
				var endpointSuffix = Arrays.stream(parts).filter(p -> p.startsWith("EndpointSuffix=")).map(p -> p.substring("EndpointSuffix=".length())).findFirst().orElse("core.windows.net");
				queueServiceEndpoint = protocol + "://" + accountName + ".queue." + endpointSuffix;
			}
		} else {
			queueServiceEndpoint = endpoint;
		}
	}
}
