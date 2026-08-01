package io.github.fortunen.kete.destinations.awseventbridge;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DescribeEventBusRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "aws-eventbridge")
public class AwsEventBridgeDestination extends Destination<AwsEventBridgeDestinationConfig> {

	private String source;
	private String eventBus;
	private String detailType;
	private boolean isSourceTemplated;
	private boolean isEventBusTemplated;
	private boolean isDetailTypeTemplated;
	private EventBridgeClient eventBridgeClient;

	@Override
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		source = config.getSource();
		eventBus = config.getEventBus();
		detailType = config.getDetailType();
		isSourceTemplated = config.isSourceTemplated();
		isEventBusTemplated = config.isEventBusTemplated();
		isDetailTypeTemplated = config.isDetailTypeTemplated();
		eventBridgeClient = config.getEventBridgeClientBuilder().build();

		// verify connection (real AWS only — emulators use endpoint-url)

		if (ValidationUtils.isBlank(config.getEndpointUrl())) {
			eventBridgeClient.describeEventBus(DescribeEventBusRequest.builder().name(eventBus).build());
		}
	}

	@Override
	public boolean isHealthy() {
		return true; // stateless HTTP client; nothing to probe
	}

	@Override
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var actualEventBus = isEventBusTemplated ? TemplateUtils.substitute(eventBus, message) : eventBus;
		var actualDetailType = isDetailTypeTemplated ? TemplateUtils.substitute(detailType, message) : detailType;
		var actualSource = isSourceTemplated ? TemplateUtils.substitute(source, message) : source;

		var entry = PutEventsRequestEntry.builder()
			.eventBusName(actualEventBus)
			.source(actualSource)
			.detailType(actualDetailType);

		var payload = encodePayload(message.eventBody());

		var builtEntry = entry.detail(new String(payload)).build();

		var request = PutEventsRequest.builder().entries(builtEntry).build();

		eventBridgeClient.putEvents(request);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(eventBridgeClient, "eventBridgeClient");
	}
}
