package io.github.fortunen.kete.unittests.destinations.awseventbridgedestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.awseventbridge.AwsEventBridgeDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class closeTests {

	private static AwsEventBridgeDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AwsEventBridgeDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseEventBridgeClient() {

		// arrange

		var eventBridgeClient = mock(EventBridgeClient.class);
		destination.setEventBridgeClient(eventBridgeClient);

		// act

		destination.close();

		// assert

		verify(eventBridgeClient).close();
	}

	@Test
	public void shouldHandleNullEventBridgeClient() {

		// arrange

		destination.setEventBridgeClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
