package io.github.fortunen.kete.unittests.destinations.natsdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.nats.NatsDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;

public class sendTests {

	private static NatsDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new NatsDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldSendMessageWithCorrectSubjectAndBody() {

		// arrange

		var connection = mock(Connection.class);
		destination.setConnection(connection);
		destination.setSubject("test-subject");
		destination.setSubjectTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(connection).publish(eq("test-subject"), any(Headers.class), eq(body));
	}

	@Test
	public void shouldSendMessageWithTemplatedSubject() {

		// arrange

		var connection = mock(Connection.class);
		destination.setConnection(connection);
		destination.setSubject("events.${realmLowerCase}");
		destination.setSubjectTemplated(true);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(connection).publish(eq("events.my-realm"), any(Headers.class), eq(body));
	}

	@Test
	public void shouldSendMessageWithCustomHeaders() {

		// arrange

		var connection = mock(Connection.class);
		destination.setConnection(connection);
		destination.setSubject("test-subject");
		destination.setSubjectTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of(Map.entry("x-custom", "custom-value")));

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-003", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(connection).publish(eq("test-subject"), any(Headers.class), eq(body));
	}

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}

	@Test
	public void shouldNotRetryPublishWhenConnectionIsHealthy() {

		// arrange

		var connection = mock(Connection.class);
		when(connection.getStatus()).thenReturn(Connection.Status.CONNECTED);
		doThrow(new IllegalStateException("Output queue is full")).when(connection).publish(anyString(), any(Headers.class), any(byte[].class));

		destination.setConnection(connection);
		destination.setSubject("test-subject");
		destination.setSubjectTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-004", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		var thrown = catchThrowable(() -> destination.doSend(message));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		verify(connection, times(1)).publish(anyString(), any(Headers.class), any(byte[].class));
	}

	@Test
	public void shouldReconnectAndRetryPublishOnceWhenConnectionClosed() {

		// arrange

		var connectCount = new AtomicInteger();
		var staleConnection = mock(Connection.class);
		var freshConnection = mock(Connection.class);

		when(staleConnection.getStatus()).thenReturn(Connection.Status.CONNECTED, Connection.Status.CLOSED);
		when(freshConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
		doThrow(new IllegalStateException("Connection is Closed")).when(staleConnection).publish(anyString(), any(Headers.class), any(byte[].class));

		var recoveringDestination = new NatsDestination() {
			@Override
			protected void connect() {
				connectCount.incrementAndGet();
				setConnection(freshConnection);
			}
		};

		recoveringDestination.setConnection(staleConnection);
		recoveringDestination.setSubject("test-subject");
		recoveringDestination.setSubjectTemplated(false);
		recoveringDestination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-005", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		recoveringDestination.doSend(message);

		// assert

		assertThat(connectCount.get()).isEqualTo(1);
		verify(staleConnection, times(1)).publish(anyString(), any(Headers.class), any(byte[].class));
		verify(freshConnection, times(1)).publish(anyString(), any(Headers.class), any(byte[].class));
	}

	@Test
	public void shouldReconnectOnlyOnceForConcurrentSendsAfterTerminalClose() throws Exception {

		// arrange

		var sendCount = 8;
		var connectCount = new AtomicInteger();
		var staleConnection = mock(Connection.class);
		var freshConnection = mock(Connection.class);

		when(staleConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
		when(freshConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);

		var recoveringDestination = new NatsDestination() {
			@Override
			protected void connect() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
				}
				connectCount.incrementAndGet();
				setConnection(freshConnection);
			}
		};

		recoveringDestination.setConnection(staleConnection);
		recoveringDestination.setSubject("test-subject");
		recoveringDestination.setSubjectTemplated(false);
		recoveringDestination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);

		// act

		try (var executor = Executors.newFixedThreadPool(sendCount)) {

			var futures = new ArrayList<Future<?>>();

			for (var i = 0; i < sendCount; i++) {
				var message = new EventMessage("test-realm", "evt-" + i, body, "LOGIN", "application/json", null, Constants.EVENT, null, null);
				futures.add(executor.submit(() -> recoveringDestination.doSend(message)));
			}

			for (var future : futures) {
				future.get(30, TimeUnit.SECONDS);
			}
		}

		// assert

		assertThat(connectCount.get()).isEqualTo(1);
		verify(staleConnection, never()).publish(anyString(), any(Headers.class), any(byte[].class));
		verify(freshConnection, times(sendCount)).publish(anyString(), any(Headers.class), any(byte[].class));
	}
}
