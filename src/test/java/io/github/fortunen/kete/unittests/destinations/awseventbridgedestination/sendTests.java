package io.github.fortunen.kete.unittests.destinations.awseventbridgedestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.awseventbridge.AwsEventBridgeDestination;
import io.github.fortunen.kete.destinations.awseventbridge.AwsEventBridgeDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;

public class sendTests {

	private static AwsEventBridgeDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AwsEventBridgeDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldPutEventsWithCorrectFields() {

		// arrange

		var eventBridgeClient = mock(EventBridgeClient.class);
		destination.setConfig(mock(AwsEventBridgeDestinationConfig.class));
		destination.setEventBridgeClient(eventBridgeClient);
		destination.setEventBus("my-bus");
		destination.setSource("kete");
		destination.setDetailType("keycloak-event");
		destination.setEventBusTemplated(false);
		destination.setSourceTemplated(false);
		destination.setDetailTypeTemplated(false);

		var body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(PutEventsRequest.class);
		verify(eventBridgeClient).putEvents(captor.capture());
		var request = captor.getValue();
		assertThat(request.entries()).hasSize(1);
		var entry = request.entries().get(0);
		assertThat(entry.eventBusName()).isEqualTo("my-bus");
		assertThat(entry.source()).isEqualTo("kete");
		assertThat(entry.detailType()).isEqualTo("keycloak-event");
		assertThat(entry.detail()).isEqualTo("{\"key\":\"value\"}");
	}

	@Test
	public void shouldPutEventsWithTemplatedEventBus() {

		// arrange

		var eventBridgeClient = mock(EventBridgeClient.class);
		destination.setConfig(mock(AwsEventBridgeDestinationConfig.class));
		destination.setEventBridgeClient(eventBridgeClient);
		destination.setEventBus("bus-${realmLowerCase}");
		destination.setSource("kete");
		destination.setDetailType("keycloak-event");
		destination.setEventBusTemplated(true);
		destination.setSourceTemplated(false);
		destination.setDetailTypeTemplated(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(PutEventsRequest.class);
		verify(eventBridgeClient).putEvents(captor.capture());
		var entry = captor.getValue().entries().get(0);
		assertThat(entry.eventBusName()).isEqualTo("bus-my-realm");
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
