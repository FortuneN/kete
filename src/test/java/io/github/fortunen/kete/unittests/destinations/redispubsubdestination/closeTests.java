package io.github.fortunen.kete.unittests.destinations.redispubsubdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.redispubsub.RedisPubSubDestination;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

@SuppressWarnings("unchecked")
public class closeTests {

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
	public void shouldCloseStandaloneResources() {

		// arrange

		var connection = mock(StatefulRedisConnection.class);
		var client = mock(RedisClient.class);
		destination.setConnection(connection);
		destination.setClient(client);

		// act

		destination.close();

		// assert

		verify(connection).close();
		verify(client).close();
	}

	@Test
	public void shouldCloseClusterResources() {

		// arrange

		var clusterConnection = mock(StatefulRedisClusterConnection.class);
		var clusterClient = mock(RedisClusterClient.class);
		destination.setClusterConnection(clusterConnection);
		destination.setClusterClient(clusterClient);

		// act

		destination.close();

		// assert

		verify(clusterConnection).close();
		verify(clusterClient).close();
	}

	@Test
	public void shouldHandleNullResources() {

		// arrange

		destination.setConnection(null);
		destination.setClient(null);
		destination.setClusterConnection(null);
		destination.setClusterClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
