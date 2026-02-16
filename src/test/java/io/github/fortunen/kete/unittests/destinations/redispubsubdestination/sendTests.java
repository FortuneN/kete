package io.github.fortunen.kete.unittests.destinations.redispubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.redispubsub.RedisPubSubDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

@SuppressWarnings("unchecked")
public class sendTests {

	private static RedisPubSubDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new RedisPubSubDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldPublishInStandaloneMode() {

		// arrange

		var commands = mock(RedisCommands.class);
		destination.setCommands(commands);
		destination.setChannel("test-channel");
		destination.setChannelTemplated(false);
		destination.setClusterMode(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(commands).publish(eq("test-channel"), eq("test-body"));
	}

	@Test
	public void shouldPublishInClusterMode() {

		// arrange

		var clusterCommands = mock(RedisAdvancedClusterCommands.class);
		destination.setClusterCommands(clusterCommands);
		destination.setChannel("test-channel");
		destination.setChannelTemplated(false);
		destination.setClusterMode(true);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(clusterCommands).publish(eq("test-channel"), eq("test-body"));
	}

	@Test
	public void shouldPublishWithTemplatedChannel() {

		// arrange

		var commands = mock(RedisCommands.class);
		destination.setCommands(commands);
		destination.setChannel("events-${realmLowerCase}");
		destination.setChannelTemplated(true);
		destination.setClusterMode(false);

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(commands).publish(eq("events-my-realm"), eq("test-body"));
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
