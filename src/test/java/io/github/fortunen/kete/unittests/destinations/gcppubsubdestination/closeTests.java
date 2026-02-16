package io.github.fortunen.kete.unittests.destinations.gcppubsubdestination;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.gcppubsub.GcpPubSubDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

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
	public void shouldHandleCloseGracefully() {

		// act & assert — should not throw

		destination.close();
	}
}
