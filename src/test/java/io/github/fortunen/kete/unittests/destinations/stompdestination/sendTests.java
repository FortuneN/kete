package io.github.fortunen.kete.unittests.destinations.stompdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

		verify(connection).sendFrame(argThat(frame -> frame.startsWith("SEND\ndestination:/queue/test\n") && frame.contains("content-type:application/json\n") && frame.contains("content-length:9\n") && frame.endsWith("\n\n")), aryEq(Arrays.copyOf(body, body.length + 1)));
	}

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

		verify(connection).sendFrame(argThat(frame -> frame.startsWith("SEND\ndestination:/queue/my-realm\n")), aryEq(Arrays.copyOf(body, body.length + 1)));
	}

	@Test
	public void shouldSendBinaryBodyUnchanged() throws Exception {

		// arrange

		var connection = mock(StompConnection.class);
		destination.setConnection(connection);
		destination.setDestination("/queue/test");
		destination.setDestinationTemplated(false);
		destination.setReceiptEnabled(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = new byte[] { 0, (byte) 0xFF, (byte) 0x80, 10, 13, 0 };
		var message = new EventMessage("test-realm", "evt-003", body, "LOGIN", "application/x-protobuf", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(connection).sendFrame(argThat(frame -> frame.contains("content-length:6\n") && frame.contains("content-type:application/x-protobuf\n")), aryEq(new byte[] { 0, (byte) 0xFF, (byte) 0x80, 10, 13, 0, 0 }));
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
