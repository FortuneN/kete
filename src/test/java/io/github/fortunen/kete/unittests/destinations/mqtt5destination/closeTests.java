package io.github.fortunen.kete.unittests.destinations.mqtt5destination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.mqtt5.Mqtt5Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static Mqtt5Destination destination;

	@BeforeAll
	static void setUp() {
		destination = new Mqtt5Destination();
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

		verify(client).disconnect();
		verify(client).close();
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

		verify(client, never()).disconnect();
		verify(client).close();
	}

	@Test
	public void shouldHandleNullClient() {

		// arrange

		destination.setClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
