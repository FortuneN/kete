package io.github.fortunen.kete.unittests.destinations.signalrdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.microsoft.signalr.HubConnection;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.signalr.SignalRDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

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
	public void shouldSendToHubMethod() {

		// arrange

		var hubConnection = mock(HubConnection.class);
		destination.setHubConnection(hubConnection);
		destination.setHubMethod("ReceiveEvent");
		destination.setHubMethodTemplated(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(hubConnection).send(eq("ReceiveEvent"), eq("test-body"));
	}

	@Test
	public void shouldSendWithTemplatedHubMethod() {

		// arrange

		var hubConnection = mock(HubConnection.class);
		destination.setHubConnection(hubConnection);
		destination.setHubMethod("Receive_${realmLowerCase}");
		destination.setHubMethodTemplated(true);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(hubConnection).send(eq("Receive_my-realm"), eq("test-body"));
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
