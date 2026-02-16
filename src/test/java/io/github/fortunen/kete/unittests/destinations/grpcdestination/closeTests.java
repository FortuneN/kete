package io.github.fortunen.kete.unittests.destinations.grpcdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.grpc.GrpcDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.grpc.ManagedChannel;

public class closeTests {

	private static GrpcDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new GrpcDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldShutdownChannel() throws Exception {

		// arrange

		var channel = mock(ManagedChannel.class);
		when(channel.shutdown()).thenReturn(channel);
		when(channel.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(true);
		destination.setChannel(channel);

		// act

		destination.close();

		// assert

		verify(channel).shutdown();
		verify(channel).awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test
	public void shouldHandleNullChannel() {

		// arrange

		destination.setChannel(null);

		// act & assert — should not throw

		destination.close();
	}
}
