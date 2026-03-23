package io.github.fortunen.kete.unittests.destinations.gcpcloudtasksdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.cloud.tasks.v2.CloudTasksGrpc;
import com.google.cloud.tasks.v2.CreateTaskRequest;

import io.github.fortunen.kete.Constants;
import io.grpc.ClientInterceptor;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.gcpcloudtasks.GcpCloudTasksDestination;
import io.github.fortunen.kete.destinations.gcpcloudtasks.GcpCloudTasksDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static GcpCloudTasksDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new GcpCloudTasksDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCreateTaskWithCorrectFields() {

		// arrange

		var stub = mock(CloudTasksGrpc.CloudTasksBlockingStub.class);
		when(stub.withInterceptors(any(ClientInterceptor[].class))).thenReturn(stub);
		destination.setConfig(mock(GcpCloudTasksDestinationConfig.class));
		destination.setStub(stub);
		destination.setQueue("my-queue");
		destination.setQueueTemplated(false);
		destination.setTargetUrl("https://example.com/handler");
		destination.setHttpMethod("POST");
		destination.setParentPathPrefix("projects/my-project/locations/us-central1/queues/");
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(CreateTaskRequest.class);
		verify(stub).createTask(captor.capture());
		var request = captor.getValue();
		assertThat(request.getParent()).isEqualTo("projects/my-project/locations/us-central1/queues/my-queue");
		var httpRequest = request.getTask().getHttpRequest();
		assertThat(httpRequest.getUrl()).isEqualTo("https://example.com/handler");
		assertThat(httpRequest.getHeadersMap()).containsEntry(Constants.MESSAGE_HEADER_EVENT_KIND, Constants.EVENT);
		assertThat(httpRequest.getHeadersMap()).containsEntry(Constants.MESSAGE_HEADER_EVENT_TYPE, "LOGIN");
		assertThat(httpRequest.getHeadersMap()).containsEntry(Constants.MESSAGE_HEADER_CONTENT_TYPE, "application/json");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
