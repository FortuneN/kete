package io.github.fortunen.kete.unittests.destinations.natsjetstreamdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import io.github.fortunen.kete.destinations.natsjetstream.NatsJetStreamDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.impl.NatsMessage;

public class sendTests {

	private static NatsJetStreamDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new NatsJetStreamDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldPublishToJetStream() throws Exception {

		// arrange

		var jetStream = mock(JetStream.class);
		destination.setJetStream(jetStream);
		destination.setSubject("test-subject");
		destination.setSubjectTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(jetStream).publish(any(NatsMessage.class));
	}

	@Test
	public void shouldPublishWithTemplatedSubject() throws Exception {

		// arrange

		var jetStream = mock(JetStream.class);
		destination.setJetStream(jetStream);
		destination.setSubject("events.${realmLowerCase}");
		destination.setSubjectTemplated(true);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(jetStream).publish(any(NatsMessage.class));
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
	public void shouldNotRetryPublishWhenConnectionIsHealthy() throws Exception {

		// arrange

		var jetStream = mock(JetStream.class);
		var connection = mock(Connection.class);
		when(connection.getStatus()).thenReturn(Connection.Status.CONNECTED);
		when(jetStream.publish(any(NatsMessage.class))).thenThrow(new IOException("Timeout or no response waiting for NATS JetStream server"));

		destination.setJetStream(jetStream);
		destination.setConnection(connection);
		destination.setSubject("test-subject");
		destination.setSubjectTemplated(false);
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-004", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		var thrown = catchThrowable(() -> destination.doSend(message));

		// assert

		assertThat(thrown).isInstanceOf(IOException.class);
		verify(jetStream, times(1)).publish(any(NatsMessage.class));
	}

	@Test
	public void shouldReconnectAndRetryPublishOnceWhenConnectionClosed() throws Exception {

		// arrange

		var connectCount = new AtomicInteger();
		var staleJetStream = mock(JetStream.class);
		var staleConnection = mock(Connection.class);
		var freshJetStream = mock(JetStream.class);
		var freshConnection = mock(Connection.class);

		when(staleConnection.getStatus()).thenReturn(Connection.Status.CONNECTED, Connection.Status.CLOSED);
		when(freshConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
		when(staleJetStream.publish(any(NatsMessage.class))).thenThrow(new IllegalStateException("Connection is Closed"));

		var recoveringDestination = new NatsJetStreamDestination() {
			@Override
			protected void connect() {
				connectCount.incrementAndGet();
				setJetStream(freshJetStream);
				setConnection(freshConnection);
			}
		};

		recoveringDestination.setJetStream(staleJetStream);
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
		verify(staleJetStream, times(1)).publish(any(NatsMessage.class));
		verify(freshJetStream, times(1)).publish(any(NatsMessage.class));
	}

	@Test
	public void shouldReconnectOnlyOnceForConcurrentSendsAfterTerminalClose() throws Exception {

		// arrange

		var sendCount = 8;
		var connectCount = new AtomicInteger();
		var staleJetStream = mock(JetStream.class);
		var staleConnection = mock(Connection.class);
		var freshJetStream = mock(JetStream.class);
		var freshConnection = mock(Connection.class);

		when(staleConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
		when(freshConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);

		var recoveringDestination = new NatsJetStreamDestination() {
			@Override
			protected void connect() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
				}
				connectCount.incrementAndGet();
				setJetStream(freshJetStream);
				setConnection(freshConnection);
			}
		};

		recoveringDestination.setJetStream(staleJetStream);
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
		verify(staleJetStream, never()).publish(any(NatsMessage.class));
		verify(freshJetStream, times(sendCount)).publish(any(NatsMessage.class));
	}
}
