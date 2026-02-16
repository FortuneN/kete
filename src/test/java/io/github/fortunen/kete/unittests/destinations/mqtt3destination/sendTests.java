package io.github.fortunen.kete.unittests.destinations.mqtt3destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.mqtt3.Mqtt3Destination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

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
	public void shouldPublishToTopic() throws Exception {

		// arrange

		var client = mock(MqttClient.class);
		destination.setClient(client);
		destination.setTopic("test/topic");
		destination.setTopicTemplated(false);
		destination.setQos(1);
		destination.setRetained(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(client).publish(eq("test/topic"), any(MqttMessage.class));
	}

	@Test
	public void shouldPublishWithTemplatedTopic() throws Exception {

		// arrange

		var client = mock(MqttClient.class);
		destination.setClient(client);
		destination.setTopic("events/${realmLowerCase}");
		destination.setTopicTemplated(true);
		destination.setQos(1);
		destination.setRetained(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(client).publish(eq("events/my-realm"), any(MqttMessage.class));
	}

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}
}
