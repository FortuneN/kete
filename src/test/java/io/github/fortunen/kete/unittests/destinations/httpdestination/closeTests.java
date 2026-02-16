package io.github.fortunen.kete.unittests.destinations.httpdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.http.HttpClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.http.HttpDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static HttpDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new HttpDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseHttpClient() throws Exception {

		// arrange

		var httpClient = mock(HttpClient.class);
		destination.setHttpClient(httpClient);

		// act

		destination.close();

		// assert

		verify(httpClient).close();
	}

	@Test
	public void shouldHandleNullHttpClient() {

		// arrange

		destination.setHttpClient(null);

		// act & assert — should not throw

		destination.close();
	}
}
