package io.github.fortunen.kete.unittests.destinations.awssqsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.awssqs.AwsSqsDestination;
import io.github.fortunen.kete.destinations.awssqs.AwsSqsDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class sendTests {

	private static AwsSqsDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AwsSqsDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendMessage() {

		// arrange

		var sqsClient = mock(SqsClient.class);
		destination.setConfig(mock(AwsSqsDestinationConfig.class));
		destination.setSqsClient(sqsClient);
		destination.setQueueUrl("https://sqs.us-east-1.amazonaws.com/123456789012/test-queue");
		destination.setQueueTemplated(false);
		destination.setHasFifoGroupId(false);
		destination.setHasFifoDeduplicationId(false);
		destination.setStaticAttributes(new HashMap<>());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(sqsClient).sendMessage(any(SendMessageRequest.class));
	}

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}
}
