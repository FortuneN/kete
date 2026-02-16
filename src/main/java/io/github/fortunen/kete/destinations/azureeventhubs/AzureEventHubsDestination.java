package io.github.fortunen.kete.destinations.azureeventhubs;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.SendOptions;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
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
@Component(name = "azure-eventhubs")
public class AzureEventHubsDestination extends Destination<AzureEventHubsDestinationConfig> {

	private String partitionId;
	private String partitionKey;
	private boolean hasPartitionId;
	private boolean hasPartitionKey;
	private boolean isPartitionKeyTemplated;
	private EventHubProducerClient producerClient;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		partitionId = config.getPartitionId();
		partitionKey = config.getPartitionKey();
		hasPartitionId = config.isHasPartitionId();
		hasPartitionKey = config.isHasPartitionKey();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();
		isPartitionKeyTemplated = config.isPartitionKeyTemplated();
		producerClient = config.getEventHubClientBuilder().buildProducerClient();

		// verify connection

		producerClient.getEventHubProperties();
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// build event data

		var eventData = new EventData(message.eventBody());

		// set message properties (standard headers)

		eventData.getProperties().put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		eventData.getProperties().put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		eventData.getProperties().put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// set custom headers

		for (var entry : customHeadersEntrySet) {
			eventData.getProperties().put(entry.getKey(), entry.getValue());
		}

		// build send options

		var sendOptions = new SendOptions();

		if (hasPartitionKey) {
			var actualPartitionKey = isPartitionKeyTemplated ? TemplateUtils.substitute(partitionKey, message) : partitionKey;
			sendOptions.setPartitionKey(actualPartitionKey);
		}

		if (hasPartitionId) {
			sendOptions.setPartitionId(partitionId);
		}

		// send

		producerClient.send(List.of(eventData), sendOptions);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(producerClient, "producerClient");
	}
}
