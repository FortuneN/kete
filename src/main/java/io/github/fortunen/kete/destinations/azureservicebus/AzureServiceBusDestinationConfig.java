package io.github.fortunen.kete.destinations.azureservicebus;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.AzureUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AzureServiceBusDestinationConfig extends DestinationConfig {

	public static final String QUEUE = "queue";
	public static final String TOPIC = "topic";
	public static final String SUBJECT = "subject";
	public static final String SESSION_ID = "session-id";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String CONNECTION_STRING = "connection-string";
	public static final String CUSTOM_ENDPOINT_ADDRESS = "custom-endpoint-address";
	public static final String FULLY_QUALIFIED_NAMESPACE = "fully-qualified-namespace";

	private String queue;
	private String topic;
	private String subject;
	private String sessionId;
	private Duration timeout;
	private boolean hasQueue;
	private boolean hasTopic;
	private boolean hasSubject;
	private int timeoutSeconds;
	private boolean hasSessionId;
	private String connectionString;
	private int serviceEndpointPort;
	private boolean isQueueTemplated;
	private boolean isTopicTemplated;
	private boolean isSubjectTemplated;
	private String serviceEndpointHost;
	private boolean isSessionIdTemplated;
	private String customEndpointAddress;
	private boolean hasCustomEndpointAddress;
	private ServiceBusClientBuilder serviceBusClientBuilder;

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

		// queue (optional — mutually exclusive with topic)

		queue = configuration.getString(QUEUE, "").trim();
		hasQueue = ValidationUtils.isNotBlank(queue);

		// topic (optional — mutually exclusive with queue)

		topic = configuration.getString(TOPIC, "").trim();
		hasTopic = ValidationUtils.isNotBlank(topic);

		// exactly one of queue or topic is required

		ValidationUtils.requireTrue(hasQueue || hasTopic, "either '" + QUEUE + "' or '" + TOPIC + "' is required for azure-servicebus destinations");
		ValidationUtils.requireFalse(hasQueue && hasTopic, "'" + QUEUE + "' and '" + TOPIC + "' are mutually exclusive — set one, not both");

		// subject (optional, templatable)

		subject = configuration.getString(SUBJECT, "").trim();
		hasSubject = ValidationUtils.isNotBlank(subject);

		// session-id (optional, templatable)

		sessionId = configuration.getString(SESSION_ID, "").trim();
		hasSessionId = ValidationUtils.isNotBlank(sessionId);

		// custom-endpoint-address (optional — for emulators)

		customEndpointAddress = configuration.getString(CUSTOM_ENDPOINT_ADDRESS, "").trim();
		hasCustomEndpointAddress = ValidationUtils.isNotBlank(customEndpointAddress);

		if (hasCustomEndpointAddress) {
			ValidationUtils.requireValidUrl(customEndpointAddress, CUSTOM_ENDPOINT_ADDRESS + " must be a valid absolute URL");
		}

		// cross-destination property rejection

		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("stream", "").trim()), "'stream' cannot be set for azure-servicebus destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("event-hub", "").trim()), "'event-hub' cannot be set for azure-servicebus destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("partition-key", "").trim()), "'partition-key' cannot be set for azure-servicebus destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("partition-id", "").trim()), "'partition-id' cannot be set for azure-servicebus destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("endpoint", "").trim()), "'endpoint' cannot be set for azure-servicebus destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("access-key", "").trim()), "'access-key' cannot be set for azure-servicebus destinations");

		// timeout

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");
		timeout = Duration.ofSeconds(timeoutSeconds);

		// precomputed fields

		isQueueTemplated = TemplateUtils.containsTemplate(queue);
		isTopicTemplated = TemplateUtils.containsTemplate(topic);
		isSubjectTemplated = TemplateUtils.containsTemplate(subject);
		isSessionIdTemplated = TemplateUtils.containsTemplate(sessionId);

		// Service Bus client builder (destination calls .sender().queueName()/topicName().buildClient())

		serviceBusClientBuilder = new ServiceBusClientBuilder();

		if (hasAuthenticationType) {
			AzureUtils.configureAuthentication(authenticationType, configuration, "connection-string",
				() -> {
					ValidationUtils.requireNonBlank(connectionString, CONNECTION_STRING + " is required when " + AUTHENTICATION_TYPE + " is 'connection-string'");
					serviceBusClientBuilder.connectionString(connectionString);
				},
				credential -> {
					ValidationUtils.requireNonBlank(fullyQualifiedNamespace, FULLY_QUALIFIED_NAMESPACE + " is required when " + AUTHENTICATION_TYPE + " is '" + authenticationType + "'");
					serviceBusClientBuilder.credential(fullyQualifiedNamespace, credential);
				}
			);
		} else {
			ValidationUtils.requireNonBlank(connectionString, "'" + CONNECTION_STRING + "' or '" + AUTHENTICATION_TYPE + "' must be set");
			serviceBusClientBuilder.connectionString(connectionString);
		}

		if (hasCustomEndpointAddress) {
			serviceBusClientBuilder.customEndpointAddress(customEndpointAddress);
		}

		// service endpoint (for connection verification via TCP — Service Bus uses AMQP protocol)

		if (hasCustomEndpointAddress) {
			var uri = URI.create(customEndpointAddress);
			serviceEndpointHost = uri.getHost();
			serviceEndpointPort = uri.getPort() != -1 ? uri.getPort() : 5672;
		} else if (hasConnectionString) {
			var endpoint = Arrays.stream(connectionString.split(";")).filter(p -> p.startsWith("Endpoint=")).map(p -> p.substring("Endpoint=".length())).findFirst().orElseThrow(() -> new IllegalStateException("connection-string must contain an Endpoint"));
			var uri = URI.create(endpoint);
			serviceEndpointHost = uri.getHost();
			serviceEndpointPort = uri.getPort() != -1 ? uri.getPort() : 5671;
		} else {
			serviceEndpointHost = fullyQualifiedNamespace;
			serviceEndpointPort = 5671;
		}
	}
}
