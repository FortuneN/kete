package io.github.fortunen.kete.unittests.destinations.httpdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.http.HttpDestination;
import io.github.fortunen.kete.destinations.http.HttpDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;

@SuppressWarnings("unchecked")
public class sendTests {

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
	public void shouldSendPostRequest() throws Exception {

		// arrange

		var httpClient = mock(HttpClient.class);
		var response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(200);
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

		destination.setConfig(mock(HttpDestinationConfig.class));
		destination.setHttpClient(httpClient);
		destination.setUrl("http://localhost:8080/webhook");
		destination.setMethod("POST");
		destination.setTimeout(Duration.ofSeconds(10));
		destination.setUrlTemplated(false);
		destination.setOauthEnabled(false);
		destination.setCustomHeaders(Set.of());

		var body = "{\"event\":\"login\"}".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
	}

	@Test
	public void shouldSendWithTemplatedUrl() throws Exception {

		// arrange

		var httpClient = mock(HttpClient.class);
		var response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(200);
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

		destination.setConfig(mock(HttpDestinationConfig.class));
		destination.setHttpClient(httpClient);
		destination.setUrl("http://localhost:8080/${realmLowerCase}/events");
		destination.setMethod("POST");
		destination.setTimeout(Duration.ofSeconds(10));
		destination.setUrlTemplated(true);
		destination.setOauthEnabled(false);
		destination.setCustomHeaders(Set.of());

		var body = "{\"event\":\"login\"}".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("my-realm", "evt-002", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
	}

	@Test
	public void shouldThrowOnNon2xxStatus() throws Exception {

		// arrange

		var httpClient = mock(HttpClient.class);
		var response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(500);
		when(response.body()).thenReturn("Internal Server Error");
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

		destination.setConfig(mock(HttpDestinationConfig.class));
		destination.setHttpClient(httpClient);
		destination.setUrl("http://localhost:8080/webhook");
		destination.setMethod("POST");
		destination.setTimeout(Duration.ofSeconds(10));
		destination.setUrlTemplated(false);
		destination.setOauthEnabled(false);
		destination.setCustomHeaders(Set.of());

		var body = "test".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-003", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		var thrown = catchThrowable(() -> destination.doSend(message));

		// assert

		assertThat(thrown).isNotNull();
	}

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}
}
