package io.github.fortunen.kete.unittests.destinations.azurewebpubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;

import io.github.fortunen.kete.destinations.azurewebpubsub.AzureWebPubSubDestination;
import io.github.fortunen.kete.utils.ValidationUtils;

public class closeTests {

	private static AzureWebPubSubDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new AzureWebPubSubDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@Test
	public void shouldClearHubClientCache() {

		// arrange

		destination.getHubClientCache().put("test-hub", mock(WebPubSubServiceClient.class));

		// act

		destination.close();

		// assert

		assertThat(destination.getHubClientCache()).isEmpty();
	}

	@Test
	public void shouldHandleEmptyCache() {

		// act & assert — should not throw

		destination.close();
	}
}
