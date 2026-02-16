package io.github.fortunen.kete.unittests.destinations.stompdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

import org.apache.activemq.transport.stomp.StompConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.stomp.StompDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static StompDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new StompDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSendToDestination() throws Exception {

		// arrange

		var connection = mock(StompConnection.class);
		destination.setConnection(connection);
		destination.setDestination("/queue/test");
		destination.setDestinationTemplated(false);
		destination.setReceiptEnabled(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(connection).send(eq("/queue/test"), any(String.class), isNull(), any(HashMap.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSendWithTemplatedDestination() throws Exception {

		// arrange

		var connection = mock(StompConnection.class);
		destination.setConnection(connection);
		destination.setDestination("/queue/${realmLowerCase}");
		destination.setDestinationTemplated(true);
		destination.setReceiptEnabled(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(connection).send(eq("/queue/my-realm"), any(String.class), isNull(), any(HashMap.class));
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
