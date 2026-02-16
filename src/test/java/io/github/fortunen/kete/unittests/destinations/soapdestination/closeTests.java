package io.github.fortunen.kete.unittests.destinations.soapdestination;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.http.HttpClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.destinations.soap.SoapDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static SoapDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new SoapDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldCloseHttpClient() {

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
