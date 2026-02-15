package io.github.fortunen.kete.destinations.awssqs;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.AwsUtils;
import io.github.fortunen.kete.utils.ConfigurationUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AwsSqsDestinationConfig extends DestinationConfig {

	public static final String QUEUE = "queue";
	public static final String SUBJECT = "subject";
	public static final String ACCOUNT_ID = "account-id";
	public static final String ATTRIBUTES = "attributes";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String ENDPOINT_URL = "endpoint-url";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String MESSAGE_GROUP_ID = "message-group-id";
	public static final String DEFAULT_EMULATOR_ACCOUNT_ID = "000000000000";
	public static final String MESSAGE_DEDUPLICATION_ID = "message-deduplication-id";

	private String queue;
	private String region;
	private String queueUrl;
	private String accountId;
	private Duration timeout;
	private int timeoutSeconds;
	private String endpointUrl;
	private String queueUrlBase;
	private String messageGroupId;
	private boolean hasFifoGroupId;
	private boolean isQueueTemplated;
	private String messageDeduplicationId;
	private boolean hasFifoDeduplicationId;
	private SqsClientBuilder sqsClientBuilder;
	private MapConfiguration attributesConfig;
	private Map<String, MessageAttributeValue> staticAttributes;

	@Override
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// endpoint-url (optional)

		endpointUrl = configuration.getString(ENDPOINT_URL, "").trim();

		var hasEndpointUrl = ValidationUtils.isNotBlank(endpointUrl);

		if (hasEndpointUrl) {
			ValidationUtils.requireValidUrl(endpointUrl, ENDPOINT_URL + " must be a valid absolute URL");
		}

		// authentication-type (optional)

		// cross-contamination validation

		if (hasAuthenticationType) {
			AwsUtils.validateAuthenticationTypeCrossContamination(authenticationType, configuration);
		}

		// region

		region = AwsUtils.resolveRegion(configuration);

		ValidationUtils.requireNonBlank(region, "region is required — set the 'region' config property or the AWS_REGION / AWS_DEFAULT_REGION environment variable");

		// queue (required)

		queue = ValidationUtils.requireNonBlank(configuration.getString(QUEUE, "").trim(), "'" + QUEUE + "' is required for aws-sqs destinations");

		// cross-destination property rejection

		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("topic", "").trim()), "'topic' cannot be set for aws-sqs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString(SUBJECT, "").trim()), "'subject' cannot be set for aws-sqs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("stream", "").trim()), "'stream' cannot be set for aws-sqs destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("partition-key", "").trim()), "'partition-key' cannot be set for aws-sqs destinations");

		// account-id

		accountId = configuration.getString(ACCOUNT_ID, "").trim();

		if (ValidationUtils.isBlank(accountId)) {
			if (hasEndpointUrl) {
				accountId = DEFAULT_EMULATOR_ACCOUNT_ID;
			} else {
				ValidationUtils.requireNonBlank(accountId, ACCOUNT_ID + " is required when connecting to real AWS (" + ENDPOINT_URL + " not set)");
			}
		}

		// FIFO rules

		messageGroupId = configuration.getString(MESSAGE_GROUP_ID, "").trim();
		messageDeduplicationId = configuration.getString(MESSAGE_DEDUPLICATION_ID, "").trim();

		var isFifo = queue.endsWith(".fifo");

		ValidationUtils.requireTrue(!ValidationUtils.isNotBlank(messageGroupId) || isFifo, "'" + MESSAGE_GROUP_ID + "' is set but queue '" + queue + "' does not end in '.fifo' — FIFO properties require a FIFO queue");
		ValidationUtils.requireTrue(!isFifo || ValidationUtils.isNotBlank(messageGroupId), "queue '" + queue + "' is a FIFO queue but '" + MESSAGE_GROUP_ID + "' is not set — FIFO queues require a message group ID");

		// timeout

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");
		timeout = Duration.ofSeconds(timeoutSeconds);

		// attributes

		attributesConfig = ConfigurationUtils.getSubSet(configuration, ATTRIBUTES);

		// queue URL

		queueUrl = AwsUtils.buildSqsQueueUrl(endpointUrl, region, accountId, queue);

		// precomputed fields

		isQueueTemplated = TemplateUtils.containsTemplate(queue);
		queueUrlBase = queueUrl.substring(0, queueUrl.lastIndexOf('/') + 1);
		hasFifoGroupId = ValidationUtils.isNotBlank(messageGroupId);
		hasFifoDeduplicationId = ValidationUtils.isNotBlank(messageDeduplicationId);

		// static attributes (user attributes + custom headers — identical every send)

		staticAttributes = new HashMap<>();

		if (ValidationUtils.isNotNull(attributesConfig)) {
			attributesConfig.getKeys().forEachRemaining(key -> {
				var value = attributesConfig.getString(key, "").trim();
				if (ValidationUtils.isNotBlank(value)) {
					staticAttributes.put(key, MessageAttributeValue.builder().dataType("String").stringValue(value).build());
				}
			});
		}

		for (var entry : customHeadersEntrySet) {
			staticAttributes.put(entry.getKey(), MessageAttributeValue.builder().dataType("String").stringValue(entry.getValue()).build());
		}

		// SQS client builder (destination calls .build())

		var credentialsProvider = AwsUtils.createCredentialsProvider(authenticationType, configuration);

		var httpClientBuilder = UrlConnectionHttpClient.builder()
			.socketTimeout(timeout)
			.connectionTimeout(timeout);

		if (tls.isEnabled()) {
			httpClientBuilder.tlsTrustManagersProvider(() -> tls.getTrustManagerFactory().getTrustManagers());
			if (ValidationUtils.isNotNull(tls.getKeyManagerFactory())) {
				httpClientBuilder.tlsKeyManagersProvider(() -> tls.getKeyManagerFactory().getKeyManagers());
			}
		}

		sqsClientBuilder = SqsClient.builder()
			.region(Region.of(region))
			.credentialsProvider(credentialsProvider)
			.httpClient(httpClientBuilder.build())
			.overrideConfiguration(c -> c.apiCallTimeout(timeout).apiCallAttemptTimeout(timeout));

		if (hasEndpointUrl) {
			sqsClientBuilder.endpointOverride(URI.create(endpointUrl));
		}
	}
}
