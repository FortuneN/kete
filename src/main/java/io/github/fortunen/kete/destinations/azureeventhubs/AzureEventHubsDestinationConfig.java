package io.github.fortunen.kete.destinations.azureeventhubs;

import java.time.Duration;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.AzureUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"connectionString"})
public class AzureEventHubsDestinationConfig extends DestinationConfig {

	public static final String EVENT_HUB = "event-hub";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String PARTITION_ID = "partition-id";
	public static final String PARTITION_KEY = "partition-key";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String CONNECTION_STRING = "connection-string";
	public static final String CUSTOM_ENDPOINT_ADDRESS = "custom-endpoint-address";
	public static final String FULLY_QUALIFIED_NAMESPACE = "fully-qualified-namespace";

	private String eventHub;
	private Duration timeout;
	private String partitionId;
	private int timeoutSeconds;
	private boolean hasTimeoutSeconds;
	private String partitionKey;
	private boolean hasEventHub;
	private boolean hasPartitionId;
	private boolean hasPartitionKey;
	private String connectionString;
	private String customEndpointAddress;
	private boolean isPartitionKeyTemplated;
	private boolean hasCustomEndpointAddress;
	private EventHubClientBuilder eventHubClientBuilder;

	@Override
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

		// fully-qualified-namespace (required for managed-identity / default-azure-credential)

		var fullyQualifiedNamespace = configuration.getString(FULLY_QUALIFIED_NAMESPACE, "").trim();

		// event-hub (optional — can be embedded in connection string)

		eventHub = configuration.getString(EVENT_HUB, "").trim();
		hasEventHub = ValidationUtils.isNotBlank(eventHub);

		// partition-key (optional, templatable)

		partitionKey = configuration.getString(PARTITION_KEY, "").trim();
		hasPartitionKey = ValidationUtils.isNotBlank(partitionKey);

		// partition-id (optional)

		partitionId = configuration.getString(PARTITION_ID, "").trim();
		hasPartitionId = ValidationUtils.isNotBlank(partitionId);

		// partition-key and partition-id are mutually exclusive

		ValidationUtils.requireFalse(hasPartitionKey && hasPartitionId, "'" + PARTITION_KEY + "' and '" + PARTITION_ID + "' are mutually exclusive — set one or neither, not both");

		// custom-endpoint-address (optional — for emulators)

		customEndpointAddress = configuration.getString(CUSTOM_ENDPOINT_ADDRESS, "").trim();
		hasCustomEndpointAddress = ValidationUtils.isNotBlank(customEndpointAddress);

		if (hasCustomEndpointAddress) {
			ValidationUtils.requireValidUrl(customEndpointAddress, CUSTOM_ENDPOINT_ADDRESS + " must be a valid absolute URL");
		}

		// cross-destination property rejection

		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("queue", "").trim()), "'queue' cannot be set for azure-eventhubs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("topic", "").trim()), "'topic' cannot be set for azure-eventhubs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("stream", "").trim()), "'stream' cannot be set for azure-eventhubs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("subject", "").trim()), "'subject' cannot be set for azure-eventhubs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("endpoint", "").trim()), "'endpoint' cannot be set for azure-eventhubs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("access-key", "").trim()), "'access-key' cannot be set for azure-eventhubs destinations");

		// timeout

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");
		timeout = Duration.ofSeconds(timeoutSeconds);
		hasTimeoutSeconds = configuration.containsKey(TIMEOUT_SECONDS);

		// precomputed fields

		isPartitionKeyTemplated = TemplateUtils.containsTemplate(partitionKey);

		// Event Hub client builder (destination calls .buildProducerClient())

		eventHubClientBuilder = new EventHubClientBuilder();

		if (hasTimeoutSeconds) {
			eventHubClientBuilder.retryOptions(new AmqpRetryOptions().setTryTimeout(timeout));
		}

		if (hasAuthenticationType) {
			AzureUtils.configureAuthentication(authenticationType, configuration, "connection-string",
				() -> {
					ValidationUtils.requireNonBlank(connectionString, CONNECTION_STRING + " is required when " + AUTHENTICATION_TYPE + " is 'connection-string'");
					if (hasEventHub) {
						eventHubClientBuilder.connectionString(connectionString, eventHub);
					} else {
						eventHubClientBuilder.connectionString(connectionString);
					}
				},
				credential -> {
					ValidationUtils.requireNonBlank(fullyQualifiedNamespace, FULLY_QUALIFIED_NAMESPACE + " is required when " + AUTHENTICATION_TYPE + " is '" + authenticationType + "'");
					ValidationUtils.requireTrue(hasEventHub, "'" + EVENT_HUB + "' is required when " + AUTHENTICATION_TYPE + " is '" + authenticationType + "'");
					eventHubClientBuilder.credential(fullyQualifiedNamespace, eventHub, credential);
				}
			);
		} else {
			ValidationUtils.requireNonBlank(connectionString, "'" + CONNECTION_STRING + "' or '" + AUTHENTICATION_TYPE + "' must be set");
			if (hasEventHub) {
				eventHubClientBuilder.connectionString(connectionString, eventHub);
			} else {
				eventHubClientBuilder.connectionString(connectionString);
			}
		}

		if (hasCustomEndpointAddress) {
			eventHubClientBuilder.customEndpointAddress(customEndpointAddress);
		}
	}
}
