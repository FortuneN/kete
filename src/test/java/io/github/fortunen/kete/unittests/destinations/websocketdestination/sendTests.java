package io.github.fortunen.kete.unittests.destinations.websocketdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.websocket.WebSocketDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static WebSocketDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new WebSocketDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendTextMessage() {

		// arrange

		var wsClient = mock(WebSocketClient.class);
		destination.setWebSocketClient(wsClient);
		destination.setUrl("ws://localhost:8080/ws");
		destination.setUrlTemplated(false);
		destination.setBinaryMode(false);
		destination.setOauthEnabled(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(wsClient).send(eq("test-body"));
		verify(wsClient, never()).addHeader(anyString(), anyString());
	}

	@Test
	public void shouldSendBinaryMessage() {

		// arrange

		var wsClient = mock(WebSocketClient.class);
		destination.setWebSocketClient(wsClient);
		destination.setUrl("ws://localhost:8080/ws");
		destination.setUrlTemplated(false);
		destination.setBinaryMode(true);
		destination.setOauthEnabled(false);

		var body = new byte[]{0x01, 0x02, 0x03};
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/octet-stream", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(wsClient).send(body);
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
