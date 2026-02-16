package io.github.fortunen.kete.unittests.destinations.awssqsdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.awssqs.AwsSqsDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.sqs.SqsClient;

public class closeTests {

	private static AwsSqsDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AwsSqsDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseSqsClient() {

		// arrange

		var sqsClient = mock(SqsClient.class);
		destination.setSqsClient(sqsClient);

		// act

		destination.close();

		// assert

		verify(sqsClient).close();
	}

	@Test
	public void shouldHandleNullSqsClient() {

		// arrange

		destination.setSqsClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
