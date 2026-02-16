package io.github.fortunen.kete.unittests.destinations.awskinesisdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.awskinesis.AwsKinesisDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.kinesis.KinesisClient;

public class closeTests {

	private static AwsKinesisDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AwsKinesisDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseKinesisClient() {

		// arrange

		var kinesisClient = mock(KinesisClient.class);
		destination.setKinesisClient(kinesisClient);

		// act

		destination.close();

		// assert

		verify(kinesisClient).close();
	}

	@Test
	public void shouldHandleNullKinesisClient() {

		// arrange

		destination.setKinesisClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
