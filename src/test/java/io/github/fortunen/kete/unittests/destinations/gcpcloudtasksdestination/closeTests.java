package io.github.fortunen.kete.unittests.destinations.gcpcloudtasksdestination;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.gcpcloudtasks.GcpCloudTasksDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.grpc.ManagedChannel;

import static org.mockito.Mockito.mock;

public class closeTests {

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
	public void shouldCloseChannel() {

		// arrange — ManagedChannel uses tryClose(Object, ...) which reflectively looks for close()

		var channel = mock(ManagedChannel.class);
		destination.setChannel(channel);

		// act & assert — should not throw

		destination.close();
	}

	@Test
	public void shouldHandleNullChannel() {

		// arrange

		destination.setChannel(null);

		// act & assert — should not throw

		destination.close();
	}
}
