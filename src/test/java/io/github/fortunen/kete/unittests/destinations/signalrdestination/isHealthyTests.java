package io.github.fortunen.kete.unittests.destinations.signalrdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionState;

import io.github.fortunen.kete.destinations.signalr.SignalRDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class isHealthyTests {

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
	public void shouldReturnTrueWhenHubConnectionIsConnected() {

		// arrange

		var hubConnection = mock(HubConnection.class);
		when(hubConnection.getConnectionState()).thenReturn(HubConnectionState.CONNECTED);
		destination.setHubConnection(hubConnection);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenHubConnectionIsDisconnected() {

		// arrange

		var hubConnection = mock(HubConnection.class);
		when(hubConnection.getConnectionState()).thenReturn(HubConnectionState.DISCONNECTED);
		destination.setHubConnection(hubConnection);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenHubConnectionIsNull() {

		// arrange

		destination.setHubConnection(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}
}
