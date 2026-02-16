package io.github.fortunen.kete.unittests.destinations.signalrdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.microsoft.signalr.HubConnection;

import io.github.fortunen.kete.destinations.signalr.SignalRDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.reactivex.rxjava3.core.Completable;

public class closeTests {

	private static SignalRDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new SignalRDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldStopHubConnection() {

		// arrange

		var hubConnection = mock(HubConnection.class);
		when(hubConnection.stop()).thenReturn(Completable.complete());
		destination.setHubConnection(hubConnection);
		destination.setTimeoutSeconds(5);

		// act

		destination.close();

		// assert

		verify(hubConnection).stop();
	}

	@Test
	public void shouldHandleNullHubConnection() {

		// arrange

		destination.setHubConnection(null);
		destination.setTimeoutSeconds(5);

		// act & assert — should not throw

		destination.close();
	}
}
