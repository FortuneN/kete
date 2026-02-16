package io.github.fortunen.kete.destinations.azureeventgrid;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "azure-eventgrid")
public class AzureEventGridDestination extends Destination<AzureEventGridDestinationConfig> {

	private String subject;
	private String eventType;
	private String dataVersion;
	private boolean hasSubject;
	private boolean isSubjectTemplated;
	private boolean isEventTypeTemplated;
	private EventGridPublisherClient<EventGridEvent> publisherClient;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		subject = config.getSubject();
		eventType = config.getEventType();
		hasSubject = config.isHasSubject();
		dataVersion = config.getDataVersion();
		isSubjectTemplated = config.isSubjectTemplated();
		isEventTypeTemplated = config.isEventTypeTemplated();
		publisherClient = config.getEventGridClientBuilder().buildEventGridEventPublisherClient();

		// verify connection

		var testHttpClientBuilder = HttpClient.newBuilder();

		if (config.getTls().isEnabled()) {
			testHttpClientBuilder.sslContext(config.getTls().getKeyStoreAndTrustStoreSSLContext());
		}

		var testRequest = HttpRequest.newBuilder().uri(URI.create(config.getEndpoint())).timeout(config.getTimeout()).GET().build();

		testHttpClientBuilder.build().send(testRequest, HttpResponse.BodyHandlers.discarding());
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// resolve subject

		var actualSubject = hasSubject ? (isSubjectTemplated ? TemplateUtils.substitute(subject, message) : subject) : message.eventType();

		// resolve event type

		var actualEventType = isEventTypeTemplated ? TemplateUtils.substitute(eventType, message) : eventType;

		// build event grid event

		var payload = encodePayload(message.eventBody());

		var event = new EventGridEvent(actualSubject, actualEventType, BinaryData.fromBytes(payload), dataVersion);

		// send

		publisherClient.sendEvent(event);
	}

	@Override
	public void close() {
		// EventGridPublisherClient is stateless HTTP — no cleanup needed
	}
}
