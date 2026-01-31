package io.github.fortunen.kete.integrationtests.amqp1destination;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class closeTests extends TestBase {

	@Test
	public void shouldCloseWithoutException() throws Exception {

		// arrange

		startActiveMqArtemis();
		var map = new HashMap<String, Object>();
		map.put("host", getHost());
		map.put("port", String.valueOf(getMappedPort()));
		map.put("destination-name", "test-queue");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		// act & assert

		assertThatCode(() -> destination.close()).doesNotThrowAnyException();
	}
}
