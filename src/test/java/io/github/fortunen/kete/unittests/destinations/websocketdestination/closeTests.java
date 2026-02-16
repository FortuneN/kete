package io.github.fortunen.kete.unittests.destinations.websocketdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.websocket.WebSocketDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

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
	public void shouldCloseClientAndClearCache() {

		// arrange

		var wsClient = mock(WebSocketClient.class);
		var cachedClient = mock(WebSocketClient.class);
		destination.setWebSocketClient(wsClient);
		var cache = new ConcurrentHashMap<String, WebSocketClient>();
		cache.put("ws://example.com", cachedClient);
		destination.setClientCache(cache);

		// act

		destination.close();

		// assert

		verify(wsClient).close();
		verify(cachedClient).close();
		assertThat(destination.getClientCache()).isEmpty();
	}

	@Test
	public void shouldHandleNullClient() {

		// arrange

		destination.setWebSocketClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
