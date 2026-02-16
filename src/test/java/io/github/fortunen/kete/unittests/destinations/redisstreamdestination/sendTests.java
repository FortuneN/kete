package io.github.fortunen.kete.unittests.destinations.redisstreamdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.redisstream.RedisStreamDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

@SuppressWarnings("unchecked")
public class sendTests {

	private static RedisStreamDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new RedisStreamDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldXaddInStandaloneMode() {

		// arrange

		var commands = mock(RedisCommands.class);
		destination.setCommands(commands);
		destination.setStream("test-stream");
		destination.setStreamTemplated(false);
		destination.setClusterMode(false);
		destination.setStaticXAddArgs(new XAddArgs());
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(commands).xadd(eq("test-stream"), any(XAddArgs.class), any(Map.class));
	}

	@Test
	public void shouldXaddInClusterMode() {

		// arrange

		var clusterCommands = mock(RedisAdvancedClusterCommands.class);
		destination.setClusterCommands(clusterCommands);
		destination.setStream("test-stream");
		destination.setStreamTemplated(false);
		destination.setClusterMode(true);
		destination.setStaticXAddArgs(new XAddArgs());
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(clusterCommands).xadd(eq("test-stream"), any(XAddArgs.class), any(Map.class));
	}

	@Test
	public void shouldXaddWithTemplatedStream() {

		// arrange

		var commands = mock(RedisCommands.class);
		destination.setCommands(commands);
		destination.setStream("events-${realmLowerCase}");
		destination.setStreamTemplated(true);
		destination.setClusterMode(false);
		destination.setStaticXAddArgs(new XAddArgs());
		destination.setCustomHeadersEntrySet(Set.of());

		var body = "test-body".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGOUT", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(commands).xadd(eq("events-my-realm"), any(XAddArgs.class), any(Map.class));
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
