package io.github.fortunen.kete.unittests.destinations.websocketdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.websocket.WebSocketDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class isHealthyTests {

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
	public void shouldReturnTrueWhenClientIsOpen() {

		// arrange

		var client = mock(WebSocketClient.class);
		when(client.isOpen()).thenReturn(true);
		destination.setUrlTemplated(false);
		destination.setWebSocketClient(client);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenClientIsNotOpen() {

		// arrange

		var client = mock(WebSocketClient.class);
		when(client.isOpen()).thenReturn(false);
		destination.setUrlTemplated(false);
		destination.setWebSocketClient(client);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenClientIsNull() {

		// arrange

		destination.setUrlTemplated(false);
		destination.setWebSocketClient(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenUrlIsTemplated() {

		// arrange

		destination.setUrlTemplated(true);
		destination.setWebSocketClient(null);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}
}
