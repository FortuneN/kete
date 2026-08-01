package io.github.fortunen.kete.unittests.destinations.mqtt3destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.mqtt3.Mqtt3Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class isHealthyTests {

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
	public void shouldReturnTrueWhenClientIsConnected() {

		// arrange

		var client = mock(MqttClient.class);
		when(client.isConnected()).thenReturn(true);
		destination.setClient(client);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenClientIsNotConnected() {

		// arrange

		var client = mock(MqttClient.class);
		when(client.isConnected()).thenReturn(false);
		destination.setClient(client);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenClientIsNull() {

		// arrange

		destination.setClient(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}
}
