package io.github.fortunen.kete.unittests.destinations.mqtt3destination;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.mqtt3.Mqtt3Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static Mqtt3Destination destination;

	@BeforeAll
	static void setUp() {
		destination = new Mqtt3Destination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldDisconnectAndCloseClient() throws Exception {

		// arrange

		var client = mock(MqttClient.class);
		when(client.isConnected()).thenReturn(true);
		destination.setClient(client);

		// act

		destination.close();

		// assert

		verify(client).disconnectForcibly(0, 1000, true);
		verify(client).close(true);
	}

	@Test
	public void shouldSkipDisconnectWhenNotConnected() throws Exception {

		// arrange

		var client = mock(MqttClient.class);
		when(client.isConnected()).thenReturn(false);
		destination.setClient(client);

		// act

		destination.close();

		// assert

		verify(client, never()).disconnectForcibly(anyLong(), anyLong(), anyBoolean());
		verify(client).close(true);
	}

	@Test
	public void shouldHandleNullClient() {

		// arrange

		destination.setClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
