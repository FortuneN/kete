package io.github.fortunen.kete.unittests.destinations.azurestoragequeuedestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestination;
import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static AzureStorageQueueDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AzureStorageQueueDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendMessageToQueue() {

		// arrange

		var queueClient = mock(QueueClient.class);
		destination.setConfig(mock(AzureStorageQueueDestinationConfig.class));
		destination.setQueueClient(queueClient);
		destination.setQueue("test-queue");
		destination.setQueueTemplated(false);
		destination.setMessageTtl(null);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(queueClient).sendMessage("test-body");
	}

	@Test
	public void shouldSendMessageWithTtl() {

		// arrange

		var queueClient = mock(QueueClient.class);
		destination.setConfig(mock(AzureStorageQueueDestinationConfig.class));
		destination.setQueueClient(queueClient);
		destination.setQueue("test-queue");
		destination.setQueueTemplated(false);
		destination.setMessageTtl(Duration.ofSeconds(30));
		destination.setTimeout(Duration.ofSeconds(5));

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(queueClient).sendMessageWithResponse(eq("test-body"), isNull(), eq(Duration.ofSeconds(30)), eq(Duration.ofSeconds(5)), eq(Context.NONE));
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
