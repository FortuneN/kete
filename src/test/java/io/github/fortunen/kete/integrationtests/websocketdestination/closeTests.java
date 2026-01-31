package io.github.fortunen.kete.integrationtests.websocketdestination;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

public class closeTests extends TestBase {

	@Test
	public void shouldCloseWithoutError() throws Exception {

		// arrange

		startWebSocketEchoServer();
		var map = new HashMap<String, Object>();
		map.put("host", container.getHost());
		map.put("port", String.valueOf(getWebSocketPort()));
		map.put("path", "/.ws");
		var mapConfig = new MapConfiguration(map);
		configureDestination(mapConfig);
		destination.initialize();

		// act & assert

		assertThatCode(() -> destination.close()).doesNotThrowAnyException();
	}
}
