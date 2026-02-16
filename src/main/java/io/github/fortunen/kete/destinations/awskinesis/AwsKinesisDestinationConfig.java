package io.github.fortunen.kete.destinations.awskinesis;

import java.net.URI;
import java.time.Duration;

import io.github.fortunen.kete.DestinationConfig;
import io.github.fortunen.kete.utils.AwsUtils;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AwsKinesisDestinationConfig extends DestinationConfig {

	public static final String STREAM = "stream";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String ENDPOINT_URL = "endpoint-url";
	public static final String PARTITION_KEY = "partition-key";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";

	private String stream;
	private String region;
	private Duration timeout;
	private String endpointUrl;
	private int timeoutSeconds;
	private String partitionKey;
	private boolean isStreamTemplated;
	private boolean isPartitionKeyTemplated;
	private KinesisClientBuilder kinesisClientBuilder;

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

		// stream (required)

		stream = ValidationUtils.requireNonBlank(configuration.getString(STREAM, "").trim(), "'" + STREAM + "' is required for aws-kinesis destinations");

		// partition-key (required)

		partitionKey = ValidationUtils.requireNonBlank(configuration.getString(PARTITION_KEY, "").trim(), "'" + PARTITION_KEY + "' is required for aws-kinesis destinations");

		// cross-destination property rejection

		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("queue", "").trim()), "'queue' cannot be set for aws-kinesis destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("topic", "").trim()), "'topic' cannot be set for aws-kinesis destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("subject", "").trim()), "'subject' cannot be set for aws-kinesis destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("message-group-id", "").trim()), "'message-group-id' cannot be set for aws-kinesis destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("message-deduplication-id", "").trim()), "'message-deduplication-id' cannot be set for aws-kinesis destinations");

		// timeout

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");
		timeout = Duration.ofSeconds(timeoutSeconds);

		// precomputed fields

		isStreamTemplated = TemplateUtils.containsTemplate(stream);
		isPartitionKeyTemplated = TemplateUtils.containsTemplate(partitionKey);

		// Kinesis client builder (destination calls .build())

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

		kinesisClientBuilder = KinesisClient.builder()
			.region(Region.of(region))
			.credentialsProvider(credentialsProvider)
			.httpClient(httpClientBuilder.build())
			.overrideConfiguration(c -> c.apiCallTimeout(timeout).apiCallAttemptTimeout(timeout));

		if (hasEndpointUrl) {
			kinesisClientBuilder.endpointOverride(URI.create(endpointUrl));
		}
	}
}
