package io.github.fortunen.kete.unittests.destinations.soapdestination;

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
import org.mockito.ArgumentCaptor;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.destinations.soap.SoapDestination;
import io.github.fortunen.kete.destinations.soap.SoapDestinationConfig;
import io.github.fortunen.kete.utils.ValidationUtils;

public class sendTests {

	private static SoapDestination destination;

	@BeforeAll
	static void setUp() {
		destination = new SoapDestination();
	}

	@AfterAll
	static void tearDown() {
		ValidationUtils.tryClose(destination, "destination");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSendSoapEnvelopeViaPost() throws Exception {

		// arrange

		var httpClient = mock(HttpClient.class);
		var httpResponse = mock(HttpResponse.class);
		when(httpResponse.statusCode()).thenReturn(200);
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

		destination.setConfig(mock(SoapDestinationConfig.class));
		destination.setHttpClient(httpClient);
		destination.setUrl("http://localhost:8080/ws");
		destination.setUrlTemplated(false);
		destination.setTimeout(Duration.ofSeconds(5));
		destination.setOauthEnabled(false);
		destination.setAuthHeaderName(null);
		destination.setSoapAction("http://example.com/Action");
		destination.setSoapNamespace("http://schemas.xmlsoap.org/soap/envelope/");
		destination.setSoapContentType("text/xml; charset=utf-8");
		destination.setCustomHeaders(Set.of());

		var body = "<MyData>test</MyData>".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		destination.doSend(message);

		// assert

		var captor = ArgumentCaptor.forClass(HttpRequest.class);
		verify(httpClient).send(captor.capture(), any(HttpResponse.BodyHandler.class));
		var request = captor.getValue();
		assertThat(request.uri().toString()).isEqualTo("http://localhost:8080/ws");
		assertThat(request.method()).isEqualTo("POST");
		assertThat(request.headers().firstValue("SOAPAction")).hasValue("http://example.com/Action");
		assertThat(request.headers().firstValue(SoapDestination.MESSAGE_HEADER_CONTENT_TYPE)).hasValue("text/xml; charset=utf-8");
		assertThat(request.headers().firstValue(SoapDestination.MESSAGE_HEADER_EVENT_KIND)).hasValue(Constants.EVENT);
		assertThat(request.headers().firstValue(SoapDestination.MESSAGE_HEADER_EVENT_TYPE)).hasValue("LOGIN");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldThrowOnNon2xxResponse() throws Exception {

		// arrange

		var httpClient = mock(HttpClient.class);
		var httpResponse = mock(HttpResponse.class);
		when(httpResponse.statusCode()).thenReturn(500);
		when(httpResponse.body()).thenReturn("Internal Server Error");
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

		destination.setConfig(mock(SoapDestinationConfig.class));
		destination.setHttpClient(httpClient);
		destination.setUrl("http://localhost:8080/ws");
		destination.setUrlTemplated(false);
		destination.setTimeout(Duration.ofSeconds(5));
		destination.setOauthEnabled(false);
		destination.setAuthHeaderName(null);
		destination.setSoapAction(null);
		destination.setSoapNamespace("http://schemas.xmlsoap.org/soap/envelope/");
		destination.setSoapContentType("text/xml");
		destination.setCustomHeaders(Set.of());

		var body = "<MyData>test</MyData>".getBytes(StandardCharsets.UTF_8);
		var message = new EventMessage("test-realm", "evt-001", body, "LOGIN", "application/json", null, Constants.EVENT, null, null);

		// act

		var thrown = catchThrowable(() -> destination.doSend(message));

		// assert

		assertThat(thrown).isNotNull();
	}

	@Test
	public void shouldRejectNullMessage() {

		// act

		var thrown = catchThrowable(() -> destination.doSend(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
	}
}
