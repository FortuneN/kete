package io.github.fortunen.kete.unittests.destinations.redisstreamdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.redisstream.RedisStreamDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

@SuppressWarnings("unchecked")
public class isHealthyTests {

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
	public void shouldReturnTrueWhenStandaloneConnectionIsOpen() {

		// arrange

		var connection = mock(StatefulRedisConnection.class);
		when(connection.isOpen()).thenReturn(true);
		destination.setClusterMode(false);
		destination.setConnection(connection);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenStandaloneConnectionIsNotOpen() {

		// arrange

		var connection = mock(StatefulRedisConnection.class);
		when(connection.isOpen()).thenReturn(false);
		destination.setClusterMode(false);
		destination.setConnection(connection);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenStandaloneConnectionIsNull() {

		// arrange

		destination.setClusterMode(false);
		destination.setConnection(null);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenClusterConnectionIsOpen() {

		// arrange

		var clusterConnection = mock(StatefulRedisClusterConnection.class);
		when(clusterConnection.isOpen()).thenReturn(true);
		destination.setClusterMode(true);
		destination.setClusterConnection(clusterConnection);

		// act & assert

		assertThat(destination.isHealthy()).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenClusterConnectionIsNotOpen() {

		// arrange

		var clusterConnection = mock(StatefulRedisClusterConnection.class);
		when(clusterConnection.isOpen()).thenReturn(false);
		destination.setClusterMode(true);
		destination.setClusterConnection(clusterConnection);

		// act & assert

		assertThat(destination.isHealthy()).isFalse();
	}
}
