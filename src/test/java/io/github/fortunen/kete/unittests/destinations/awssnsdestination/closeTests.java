package io.github.fortunen.kete.unittests.destinations.awssnsdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.awssns.AwsSnsDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.sns.SnsClient;

public class closeTests {

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
	public void shouldCloseSnsClient() {

		// arrange

		var snsClient = mock(SnsClient.class);
		destination.setSnsClient(snsClient);

		// act

		destination.close();

		// assert

		verify(snsClient).close();
	}

	@Test
	public void shouldHandleNullSnsClient() {

		// arrange

		destination.setSnsClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
