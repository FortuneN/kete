package io.github.fortunen.kete.integrationtests.amqp091destination;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class closeTests extends TestBase {

	@Test
	public void shouldCloseWithoutException() throws Exception {

		// arrange

		startRabbitMq();
		var map = new HashMap<String, Object>();
		map.put("host", container.getHost());
		map.put("port", String.valueOf(container.getAmqpPort()));
		map.put("exchange", "test-exchange");
		map.put("routing-key", "test-key");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		// act & assert

		assertThatCode(() -> destination.close()).doesNotThrowAnyException();
	}
}
