package io.github.fortunen.kete.integrationtests.stompdestination;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class closeTests extends TestBase {

	@Test
	public void shouldCloseWithoutError() throws Exception {

		// arrange

		startActiveMq();
		var stompDestination = "/queue/test-events";
		var map = new HashMap<String, Object>();
		map.put("host", container.getHost());
		map.put("port", String.valueOf(getStompPort()));
		map.put("destination", stompDestination);
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		// act & assert

		assertThatCode(() -> destination.close()).doesNotThrowAnyException();
	}
}
