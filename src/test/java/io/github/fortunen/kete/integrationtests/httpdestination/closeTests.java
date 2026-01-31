package io.github.fortunen.kete.integrationtests.httpdestination;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;

public class closeTests extends TestBase {

	@Test
	public void shouldCloseWithoutException() throws Exception {

		// arrange

		startMockServer();
		mockServer.enqueue(new MockResponse().setResponseCode(200));
		configureDestinationWithMockServer();
		destination.initialize();

		// act & assert

		assertThatCode(() -> destination.close()).doesNotThrowAnyException();
	}
}
