package io.github.fortunen.kete.unittests.destinations.awskinesisdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.awskinesis.AwsKinesisDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

public class sendTests {

	private static AwsKinesisDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AwsKinesisDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldPutRecordWithCorrectStreamAndPartitionKey() {

		// arrange

		var kinesisClient = mock(KinesisClient.class);
		destination.setKinesisClient(kinesisClient);
		destination.setStream("my-stream");
		destination.setPartitionKey("my-key");
		destination.setStreamTemplated(false);
		destination.setPartitionKeyTemplated(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(PutRecordRequest.class);
		verify(kinesisClient).putRecord(captor.capture());
		var request = captor.getValue();
		assertThat(request.streamName()).isEqualTo("my-stream");
		assertThat(request.partitionKey()).isEqualTo("my-key");
		assertThat(request.data().asByteArray()).isEqualTo(body);
	}

	@Test
	public void shouldPutRecordWithTemplatedStream() {

		// arrange

		var kinesisClient = mock(KinesisClient.class);
		destination.setKinesisClient(kinesisClient);
		destination.setStream("stream-${realmLowerCase}");
		destination.setPartitionKey("my-key");
		destination.setStreamTemplated(true);
		destination.setPartitionKeyTemplated(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(PutRecordRequest.class);
		verify(kinesisClient).putRecord(captor.capture());
		assertThat(captor.getValue().streamName()).isEqualTo("stream-my-realm");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
