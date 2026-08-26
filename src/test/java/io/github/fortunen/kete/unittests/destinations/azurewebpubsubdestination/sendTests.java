package io.github.fortunen.kete.unittests.destinations.azurewebpubsubdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.azurewebpubsub.AzureWebPubSubDestination;
import io.github.fortunen.kete.destinations.azurewebpubsub.AzureWebPubSubDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

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
	public void shouldSendBinaryPayloadAsBinaryMessage() {

		// arrange

		var webPubSubClient = mock(WebPubSubServiceClient.class);
		destination.setConfig(mock(AzureWebPubSubDestinationConfig.class));
		destination.setWebPubSubClient(webPubSubClient);
		destination.setHub("test_hub");
		destination.setHasGroup(false);
		destination.setHubTemplated(false);
		destination.setGroupTemplated(false);

		var body = new byte[] { 0, (byte) 0xFF, 1, 2, 3 };
		var message = new EventMessage("test-realm", "evt-002", body, "LOGIN", "application/x-protobuf", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(webPubSubClient).sendToAllWithResponse(any(BinaryData.class), eq(WebPubSubContentType.APPLICATION_OCTET_STREAM), eq(5L), any(RequestOptions.class));
	}

	@Test
	public void shouldSendToGroup() {

		// arrange

		var webPubSubClient = mock(WebPubSubServiceClient.class);
		destination.setConfig(mock(AzureWebPubSubDestinationConfig.class));
		destination.setWebPubSubClient(webPubSubClient);
		destination.setHub("test-hub");
		destination.setGroup("test-group");
		destination.setHasGroup(true);
		destination.setHubTemplated(false);
		destination.setGroupTemplated(false);

		var body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(webPubSubClient).sendToGroup(eq("test-group"), eq("{\"key\":\"value\"}"), eq(WebPubSubContentType.APPLICATION_JSON));
	}

	@Test
	public void shouldSendToAllWhenNoGroup() {

		// arrange

		var webPubSubClient = mock(WebPubSubServiceClient.class);
		destination.setConfig(mock(AzureWebPubSubDestinationConfig.class));
		destination.setWebPubSubClient(webPubSubClient);
		destination.setHub("test-hub");
		destination.setHasGroup(false);
		destination.setHubTemplated(false);
		destination.setGroupTemplated(false);
		destination.setGroup(null);

		var body = "plain text".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "text/plain", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(webPubSubClient).sendToAll(eq("plain text"), eq(WebPubSubContentType.TEXT_PLAIN));
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
