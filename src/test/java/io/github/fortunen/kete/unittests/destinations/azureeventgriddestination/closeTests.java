package io.github.fortunen.kete.unittests.destinations.azureeventgriddestination;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.azureeventgrid.AzureEventGridDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static AzureEventGridDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AzureEventGridDestination();
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
