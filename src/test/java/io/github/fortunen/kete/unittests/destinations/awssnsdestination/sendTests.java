package io.github.fortunen.kete.unittests.destinations.awssnsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.awssns.AwsSnsDestination;
import io.github.fortunen.kete.destinations.awssns.AwsSnsDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class sendTests {

	private static AwsSnsDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AwsSnsDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldPublishMessageToTopicArn() {

		// arrange

		var snsClient = mock(SnsClient.class);
		destination.setConfig(mock(AwsSnsDestinationConfig.class));
		destination.setSnsClient(snsClient);
		destination.setTopicArn("arn:aws:sns:us-east-1:123456789:test-topic");
		destination.setTopic("test-topic");
		destination.setTopicTemplated(false);
		destination.setHasSubject(false);
		destination.setHasFifoGroupId(false);
		destination.setHasFifoDeduplicationId(false);
		destination.setStaticAttributes(new HashMap<>());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(PublishRequest.class);
		verify(snsClient).publish(captor.capture());
		var request = captor.getValue();
		assertThat(request.topicArn()).isEqualTo("arn:aws:sns:us-east-1:123456789:test-topic");
		assertThat(request.message()).isEqualTo("test-body");
		assertThat(request.messageAttributes()).containsKey(Constants.MESSAGE_HEADER_EVENT_KIND);
		assertThat(request.messageAttributes()).containsKey(Constants.MESSAGE_HEADER_EVENT_TYPE);
		assertThat(request.messageAttributes()).containsKey(Constants.MESSAGE_HEADER_CONTENT_TYPE);
	}

	@Test
	public void shouldPublishMessageWithTemplatedTopic() {

		// arrange

		var snsClient = mock(SnsClient.class);
		destination.setConfig(mock(AwsSnsDestinationConfig.class));
		destination.setSnsClient(snsClient);
		destination.setTopic("events-${realmLowerCase}");
		destination.setTopicArn("arn:aws:sns:us-east-1:123456789:events-${realmLowerCase}");
		destination.setTopicArnPrefix("arn:aws:sns:us-east-1:123456789:");
		destination.setTopicTemplated(true);
		destination.setHasSubject(false);
		destination.setHasFifoGroupId(false);
		destination.setHasFifoDeduplicationId(false);
		destination.setStaticAttributes(new HashMap<>());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(PublishRequest.class);
		verify(snsClient).publish(captor.capture());
		var request = captor.getValue();
		assertThat(request.topicArn()).isEqualTo("arn:aws:sns:us-east-1:123456789:events-my-realm");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
