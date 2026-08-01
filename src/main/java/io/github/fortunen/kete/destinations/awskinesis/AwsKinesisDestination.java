package io.github.fortunen.kete.destinations.awskinesis;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

@Data
@Component(name = "aws-kinesis")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class AwsKinesisDestination extends Destination<AwsKinesisDestinationConfig> {

	private String stream;
	private String partitionKey;
	private boolean isStreamTemplated;
	private KinesisClient kinesisClient;
	private boolean isPartitionKeyTemplated;

	@Override
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

        stream = config.getStream();
		partitionKey = config.getPartitionKey();
		isStreamTemplated = config.isStreamTemplated();
		kinesisClient = config.getKinesisClientBuilder().build();
		isPartitionKeyTemplated = config.isPartitionKeyTemplated();

		// verify connection

		kinesisClient.listStreams(r -> r.limit(1));
	}

	@Override
	public boolean isHealthy() {
		return true; // stateless HTTP client; nothing to probe
	}

	@Override
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		var actualStream = isStreamTemplated ? TemplateUtils.substitute(stream, message) : stream;
		var actualPartitionKey = isPartitionKeyTemplated ? TemplateUtils.substitute(partitionKey, message) : partitionKey;
		var request = PutRecordRequest.builder().streamName(actualStream).partitionKey(actualPartitionKey).data(SdkBytes.fromByteArray(message.eventBody())).build();

		kinesisClient.putRecord(request);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(kinesisClient, "kinesisClient");
	}
}
