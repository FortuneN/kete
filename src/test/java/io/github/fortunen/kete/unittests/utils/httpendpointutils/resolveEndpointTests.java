package io.github.fortunen.kete.unittests.utils.httpendpointutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.TlsMaterial;
import io.github.fortunen.kete.utils.HttpEndpointUtils;

public class resolveEndpointTests {

	private static MapConfiguration configuration(Map<String, Object> values) {
		return new MapConfiguration(new HashMap<>(values));
	}

	@Test
	public void shouldResolveHttpsUrlAndTurnTlsOn() {

		// act

		var endpoint = HttpEndpointUtils.resolveEndpoint(configuration(Map.of("url", "https://api.example/events?x=1")), TlsMaterial.builder().build());

		// assert

		assertThat(endpoint.scheme()).isEqualTo("https");
		assertThat(endpoint.host()).isEqualTo("api.example");
		assertThat(endpoint.port()).isEqualTo(443);
		assertThat(endpoint.pathAndQuery()).isEqualTo("/events?x=1");
		assertThat(endpoint.url()).isEqualTo("https://api.example:443/events?x=1");
		assertThat(endpoint.tlsFromUrl()).isTrue();
		assertThat(endpoint.tls().isEnabled()).isTrue();
		assertThat(endpoint.urlTemplated()).isFalse();
	}

	@Test
	public void shouldResolveHostPortAndPathWhenNoUrlIsGiven() {

		// act

		var endpoint = HttpEndpointUtils.resolveEndpoint(configuration(Map.of("host", "api.example", "port", 8080, "path-and-query", "events")), TlsMaterial.builder().build());

		// assert

		assertThat(endpoint.scheme()).isEqualTo("http");
		assertThat(endpoint.port()).isEqualTo(8080);
		assertThat(endpoint.pathAndQuery()).isEqualTo("/events");
		assertThat(endpoint.url()).isEqualTo("http://api.example:8080/events");
		assertThat(endpoint.parsedUri()).isNull();
	}

	@Test
	public void shouldKeepTemplatedUrlPlaceholders() {

		// act

		var endpoint = HttpEndpointUtils.resolveEndpoint(configuration(Map.of("url", "http://api.example/${realmLowerCase}/events")), TlsMaterial.builder().build());

		// assert

		assertThat(endpoint.url()).isEqualTo("http://api.example/${realmLowerCase}/events");
		assertThat(endpoint.urlTemplated()).isTrue();
	}

	@Test
	public void shouldRejectUnsupportedScheme() {

		// act

		var thrown = catchThrowable(() -> HttpEndpointUtils.resolveEndpoint(configuration(Map.of("url", "ftp://api.example/events")), TlsMaterial.builder().build()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("url scheme must be 'http' or 'https'");
	}

	@Test
	public void shouldBuildBasicAuthenticationHeader() {

		// act

		var authentication = HttpEndpointUtils.resolveAuthentication(configuration(Map.of("basic-username", "alice", "basic-password", "secret")), "basic", null, null);

		// assert

		assertThat(authentication.headerName()).isEqualTo("Authorization");
		assertThat(authentication.headerValue()).isEqualTo("Basic YWxpY2U6c2VjcmV0");
		assertThat(authentication.oauth()).isNull();
		assertThat(authentication.oauthEnabled()).isFalse();
	}

	@Test
	public void shouldReturnNoAuthenticationWithoutAuthenticationType() {

		// act

		var authentication = HttpEndpointUtils.resolveAuthentication(configuration(Map.of()), null, null, null);

		// assert

		assertThat(authentication.headerName()).isNull();
		assertThat(authentication.headerValue()).isNull();
	}

	@Test
	public void shouldDropHeadersTheDestinationSetsItself() {

		// act

		var filtered = HttpEndpointUtils.filterCustomHeaders(Set.of(Map.entry("Content-Type", "text/plain"), Map.entry("X-EventType", "x"), Map.entry("X-Custom", "keep")));

		// assert

		assertThat(filtered).containsExactly(Map.entry("X-Custom", "keep"));
	}
}
