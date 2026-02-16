package io.github.fortunen.kete.destinations.azureeventgrid;

import java.time.Duration;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

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
public class AzureEventGridDestinationConfig extends DestinationConfig {

	public static final String SUBJECT = "subject";
	public static final String ENDPOINT = "endpoint";
	public static final String ACCESS_KEY = "access-key";
	public static final String EVENT_TYPE = "event-type";
	public static final int DEFAULT_TIMEOUT_SECONDS = 10;
	public static final String DEFAULT_DATA_VERSION = "1.0";
	public static final String DATA_VERSION = "data-version";
	public static final String TIMEOUT_SECONDS = "timeout-seconds";
	public static final String DEFAULT_EVENT_TYPE = "KeycloakEvent";

	private String subject;
	private String endpoint;
	private String accessKey;
	private String eventType;
	private Duration timeout;
	private String dataVersion;
	private int timeoutSeconds;
	private boolean hasSubject;
	private HttpClient httpClient;
	private boolean isSubjectTemplated;
	private boolean isEventTypeTemplated;
	private EventGridPublisherClientBuilder eventGridClientBuilder;

	@Override
	@SneakyThrows
	protected void doInitialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		// endpoint (required)

		endpoint = ValidationUtils.requireNonBlank(configuration.getString(ENDPOINT, "").trim(), ENDPOINT + " is required");
		ValidationUtils.requireValidUrl(endpoint, ENDPOINT + " must be a valid absolute URL");

		// access-key (conditional)

		accessKey = configuration.getString(ACCESS_KEY, "").trim();

		// authentication-type (optional)

		var hasAccessKey = ValidationUtils.isNotBlank(accessKey);

		if (hasAuthenticationType) {
			ValidationUtils.requireTrue(!hasAccessKey || authenticationType.equals("access-key"), "'" + ACCESS_KEY + "' cannot be set when '" + AUTHENTICATION_TYPE + "' is '" + authenticationType + "'");
		}

		// subject (optional, templatable)

		subject = configuration.getString(SUBJECT, "").trim();
		hasSubject = ValidationUtils.isNotBlank(subject);

		// event-type (optional, defaults to "KeycloakEvent", templatable)

		eventType = configuration.getString(EVENT_TYPE, DEFAULT_EVENT_TYPE).trim();
		ValidationUtils.requireNonBlank(eventType, EVENT_TYPE + " cannot be blank");

		// data-version (optional, defaults to "1.0")

		dataVersion = configuration.getString(DATA_VERSION, DEFAULT_DATA_VERSION).trim();
		ValidationUtils.requireNonBlank(dataVersion, DATA_VERSION + " cannot be blank");

		// cross-destination property rejection

		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("queue", "").trim()), "'queue' cannot be set for azure-eventgrid destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("topic", "").trim()), "'topic' cannot be set for azure-eventgrid destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("stream", "").trim()), "'stream' cannot be set for azure-eventgrid destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("event-hub", "").trim()), "'event-hub' cannot be set for azure-eventgrid destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("session-id", "").trim()), "'session-id' cannot be set for azure-eventgrid destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("partition-id", "").trim()), "'partition-id' cannot be set for azure-eventgrid destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("partition-key", "").trim()), "'partition-key' cannot be set for azure-eventgrid destinations");
		ValidationUtils.requireTrue(ValidationUtils.isBlank(configuration.getString("connection-string", "").trim()), "'connection-string' cannot be set for azure-eventgrid destinations");

		// timeout

		timeoutSeconds = ValidationUtils.requirePositive(configuration.getInt(TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS), TIMEOUT_SECONDS + " must be positive");
		timeout = Duration.ofSeconds(timeoutSeconds);

		// precomputed fields

		isSubjectTemplated = TemplateUtils.containsTemplate(subject);
		isEventTypeTemplated = TemplateUtils.containsTemplate(eventType);

		// Azure HTTP client (with optional TLS)

		httpClient = AzureUtils.createHttpClient(tls, timeout);

		// Event Grid client builder (destination calls .buildEventGridEventPublisherClient())

		eventGridClientBuilder = new EventGridPublisherClientBuilder()
			.endpoint(endpoint)
			.httpClient(httpClient);

		if (hasAuthenticationType) {
			AzureUtils.configureAuthentication(authenticationType, configuration, "access-key",
				() -> {
					ValidationUtils.requireNonBlank(accessKey, ACCESS_KEY + " is required when " + AUTHENTICATION_TYPE + " is 'access-key'");
					eventGridClientBuilder.credential(new AzureKeyCredential(accessKey));
				},
				credential -> eventGridClientBuilder.credential(credential)
			);
		} else {
			ValidationUtils.requireNonBlank(accessKey, "'" + ACCESS_KEY + "' or '" + AUTHENTICATION_TYPE + "' must be set");
			eventGridClientBuilder.credential(new AzureKeyCredential(accessKey));
		}
	}
}
