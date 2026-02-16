package io.github.fortunen.kete.unittests.destinations.natsjetstreamdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.natsjetstream.NatsJetStreamDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
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
}
