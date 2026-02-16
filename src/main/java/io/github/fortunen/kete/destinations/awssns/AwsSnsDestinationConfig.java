package io.github.fortunen.kete.destinations.awssns;

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
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AwsSnsDestinationConfig extends DestinationConfig {

	public static final String TOPIC = "topic";
	public static final String SUBJECT = "subject";
	public static final String ACCOUNT_ID = "account-id";
	public static final String ATTRIBUTES = "attributes";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String ENDPOINT_URL = "endpoint-url";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String MESSAGE_GROUP_ID = "message-group-id";
	public static final String DEFAULT_EMULATOR_ACCOUNT_ID = "000000000000";
	public static final String MESSAGE_DEDUPLICATION_ID = "message-deduplication-id";

	private String topic;
	private String region;
	private String subject;
	private String topicArn;
	private String accountId;
	private Duration timeout;
	private int timeoutSeconds;
	private String endpointUrl;
	private boolean hasSubject;
	private String messageGroupId;
	private String topicArnPrefix;
	private boolean hasFifoGroupId;
	private boolean isTopicTemplated;
	private String messageDeduplicationId;
	private boolean hasFifoDeduplicationId;
	private SnsClientBuilder snsClientBuilder;
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

		// topic (required)

		topic = ValidationUtils.requireNonBlank(configuration.getString(TOPIC, "").trim(), "'" + TOPIC + "' is required for aws-sns destinations");

		// subject (optional)

		subject = configuration.getString(SUBJECT, "").trim();

		// cross-destination property rejection

		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("queue", "").trim()), "'queue' cannot be set for aws-sns destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("stream", "").trim()), "'stream' cannot be set for aws-sns destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("partition-key", "").trim()), "'partition-key' cannot be set for aws-sns destinations");

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

		var isFifo = topic.endsWith(".fifo");

		ValidationUtils.requireTrue(!ValidationUtils.isNotBlank(messageGroupId) || isFifo, "'" + MESSAGE_GROUP_ID + "' is set but topic '" + topic + "' does not end in '.fifo' — FIFO properties require a FIFO topic");
		ValidationUtils.requireTrue(!isFifo || ValidationUtils.isNotBlank(messageGroupId), "topic '" + topic + "' is a FIFO topic but '" + MESSAGE_GROUP_ID + "' is not set — FIFO topics require a message group ID");

		// timeout

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");
		timeout = Duration.ofSeconds(timeoutSeconds);

		// attributes

		attributesConfig = ConfigurationUtils.getSubSet(configuration, ATTRIBUTES);

		// topic ARN

		topicArn = AwsUtils.buildSnsTopicArn(region, accountId, topic);

		// precomputed fields

		isTopicTemplated = TemplateUtils.containsTemplate(topic);
		topicArnPrefix = topicArn.substring(0, topicArn.lastIndexOf(':') + 1);
		hasSubject = ValidationUtils.isNotBlank(subject);
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

		// SNS client builder (destination calls .build())

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

		snsClientBuilder = SnsClient.builder()
			.region(Region.of(region))
			.credentialsProvider(credentialsProvider)
			.httpClient(httpClientBuilder.build())
			.overrideConfiguration(c -> c.apiCallTimeout(timeout).apiCallAttemptTimeout(timeout));

		if (hasEndpointUrl) {
			snsClientBuilder.endpointOverride(URI.create(endpointUrl));
		}
	}
}
