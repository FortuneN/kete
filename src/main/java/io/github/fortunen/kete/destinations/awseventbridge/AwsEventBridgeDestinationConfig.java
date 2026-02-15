package io.github.fortunen.kete.destinations.awseventbridge;

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
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AwsEventBridgeDestinationConfig extends DestinationConfig {

	public static final String SOURCE = "source";
	public static final String EVENT_BUS = "event-bus";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String DETAIL_TYPE = "detail-type";
	public static final String ENDPOINT_URL = "endpoint-url";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";

	private String source;
	private String region;
	private String eventBus;
	private Duration timeout;
	private String detailType;
	private String endpointUrl;
	private int timeoutSeconds;
	private boolean isSourceTemplated;
	private boolean isEventBusTemplated;
	private boolean isDetailTypeTemplated;
	private EventBridgeClientBuilder eventBridgeClientBuilder;

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

		// event-bus (required)

		eventBus = ValidationUtils.requireNonBlank(configuration.getString(EVENT_BUS, "").trim(), "'" + EVENT_BUS + "' is required for aws-eventbridge destinations");

		// source (required)

		source = ValidationUtils.requireNonBlank(configuration.getString(SOURCE, "").trim(), "'" + SOURCE + "' is required for aws-eventbridge destinations");

		// detail-type (required)

		detailType = ValidationUtils.requireNonBlank(configuration.getString(DETAIL_TYPE, "").trim(), "'" + DETAIL_TYPE + "' is required for aws-eventbridge destinations");

		// cross-destination property rejection

		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("queue", "").trim()), "'queue' cannot be set for aws-eventbridge destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("topic", "").trim()), "'topic' cannot be set for aws-eventbridge destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("stream", "").trim()), "'stream' cannot be set for aws-eventbridge destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("subject", "").trim()), "'subject' cannot be set for aws-eventbridge destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("partition-key", "").trim()), "'partition-key' cannot be set for aws-eventbridge destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("connection-string", "").trim()), "'connection-string' cannot be set for aws-eventbridge destinations");

		// timeout

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");
		timeout = Duration.ofSeconds(timeoutSeconds);

		// precomputed fields

		isSourceTemplated = TemplateUtils.containsTemplate(source);
		isEventBusTemplated = TemplateUtils.containsTemplate(eventBus);
		isDetailTypeTemplated = TemplateUtils.containsTemplate(detailType);

		// EventBridge client builder (destination calls .build())

		var credentialsProvider = AwsUtils.createCredentialsProvider(authenticationType, configuration);
		var httpClientBuilder = UrlConnectionHttpClient.builder().socketTimeout(timeout).connectionTimeout(timeout);

		if (tls.isEnabled()) {
			httpClientBuilder.tlsTrustManagersProvider(() -> tls.getTrustManagerFactory().getTrustManagers());
			if (ValidationUtils.isNotNull(tls.getKeyManagerFactory())) {
				httpClientBuilder.tlsKeyManagersProvider(() -> tls.getKeyManagerFactory().getKeyManagers());
			}
		}

		eventBridgeClientBuilder = EventBridgeClient.builder()
			.region(Region.of(region))
			.credentialsProvider(credentialsProvider)
			.httpClient(httpClientBuilder.build())
			.overrideConfiguration(c -> c.apiCallTimeout(timeout).apiCallAttemptTimeout(timeout));

		if (hasEndpointUrl) {
			eventBridgeClientBuilder.endpointOverride(URI.create(endpointUrl));
		}
	}
}
