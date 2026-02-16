package io.github.fortunen.kete.unittests.destinations.gcppubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.gcppubsub.GcpPubSubDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static GcpPubSubDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new GcpPubSubDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldPublishMessageToTopic() throws Exception {

		// arrange — mock the chained call: pubsub.projects().topics().publish(fullTopicName, publishRequest).execute()

		var publishResponse = new PublishResponse().setMessageIds(List.of("msg-1"));

		var publishCall = mock(Pubsub.Projects.Topics.Publish.class);
		when(publishCall.execute()).thenReturn(publishResponse);

		var topics = mock(Pubsub.Projects.Topics.class);
		when(topics.publish(any(String.class), any(PublishRequest.class))).thenReturn(publishCall);

		var projects = mock(Pubsub.Projects.class);
		when(projects.topics()).thenReturn(topics);

		var pubsub = mock(Pubsub.class);
		when(pubsub.projects()).thenReturn(projects);

		destination.setPubsub(pubsub);
		destination.setTopic("test-topic");
		destination.setTopicTemplated(false);
		destination.setPublishTopicPrefix("projects/my-project/topics/");
		destination.setHasOrderingKey(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var topicCaptor = ArgumentCaptor.forClass(String.class);
		var requestCaptor = ArgumentCaptor.forClass(PublishRequest.class);
		verify(topics).publish(topicCaptor.capture(), requestCaptor.capture());
		assertThat(topicCaptor.getValue()).isEqualTo("projects/my-project/topics/test-topic");
		var pubsubMessage = requestCaptor.getValue().getMessages().get(0);
		assertThat(pubsubMessage.getAttributes()).containsEntry(Constants.MESSAGE_HEADER_EVENT_KIND, Constants.EVENT);
		assertThat(pubsubMessage.getAttributes()).containsEntry(Constants.MESSAGE_HEADER_EVENT_TYPE, "LOGIN");
		assertThat(pubsubMessage.getAttributes()).containsEntry(Constants.MESSAGE_HEADER_CONTENT_TYPE, "application/json");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
