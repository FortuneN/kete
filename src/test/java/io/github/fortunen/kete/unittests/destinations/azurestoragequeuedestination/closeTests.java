package io.github.fortunen.kete.unittests.destinations.azurestoragequeuedestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.azure.storage.queue.QueueClient;

import io.github.fortunen.kete.destinations.azurestoragequeue.AzureStorageQueueDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

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
	public void shouldClearQueueClientCache() {

		// arrange

		destination.getQueueClientCache().put("test-queue", mock(QueueClient.class));

		// act

		destination.close();

		// assert

		assertThat(destination.getQueueClientCache()).isEmpty();
	}

	@Test
	public void shouldHandleEmptyCache() {

		// act & assert — should not throw

		destination.close();
	}
}
