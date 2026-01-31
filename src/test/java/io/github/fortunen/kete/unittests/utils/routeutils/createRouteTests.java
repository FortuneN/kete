package io.github.fortunen.kete.unittests.utils.routeutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.MatchMode;
import io.github.fortunen.kete.destinations.http.HttpDestinationConfig;
import io.github.fortunen.kete.serializers.JsonSerializer;
import io.github.fortunen.kete.utils.RouteUtils;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class createRouteTests {

	@Test
	void shouldThrowWhenNameIsNull() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoute(null, config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("name is required");
	}

	@Test
	void shouldThrowWhenNameIsEmpty() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoute("", config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("name is required");
	}

	@Test
	void shouldThrowWhenNameIsBlank() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoute("   ", config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("name is required");
	}

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoute("test", null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldReturnNullWhenEnabledIsFalse() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("enabled", "false");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test", config);

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldUseJsonSerializerWhenSerializerNotSpecified() {

		// arrange
		// realm-matchers is optional (omission means all realms)

		var map = new HashMap<String, Object>();
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test", config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getSerializer()).isInstanceOf(JsonSerializer.class);
	}

	@Test
	void shouldThrowWhenDestinationIsMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("serializer.kind", "json");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoute("test", config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("kind is required");
	}

	@Test
	void shouldCreateMinimalRoute() {

		// arrange
		// Minimal route with destination only (realm-matchers optional, serializer defaults to json)

		var map = new HashMap<String, Object>();
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("test-route");
		assertThat(result.getRealmMatchers()).isEmpty();
		assertThat(result.getEventMatchers()).isEmpty();
		assertThat(result.acceptRealm("any-realm")).isTrue();
		assertThat(result.acceptEvent("any-event")).isTrue();
		assertThat(result.getSerializer()).isInstanceOf(JsonSerializer.class);
		assertThat(result.getDestinationConfig()).isInstanceOf(HttpDestinationConfig.class);
		assertThat(result.getRetry()).isNull();
	}

	@Test
	void shouldCreateRouteWithRealmMatchers() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("realm-matchers.prod", "list:master,staging");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getRealmMatchers()).hasSize(1);
		assertThat(result.getRealmMatchers()[0].getName()).isEqualTo("prod");
		assertThat(result.acceptRealm("master")).isTrue();
		assertThat(result.acceptRealm("staging")).isTrue();
		assertThat(result.acceptRealm("dev")).isFalse();
	}

	@Test
	void shouldCreateRouteWithEventMatchers() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("event-matchers.events", "list:LOGIN,LOGOUT");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getEventMatchers()).hasSize(1);
		assertThat(result.getEventMatchers()[0].getName()).isEqualTo("events");
		assertThat(result.acceptEvent("LOGIN")).isTrue();
		assertThat(result.acceptEvent("LOGOUT")).isTrue();
		assertThat(result.acceptEvent("REGISTER")).isFalse();
	}

	@Test
	void shouldCreateRouteWithMultipleRealmMatchers() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("realm-matchers.production", "list:master");
		map.put("realm-matchers.staging", "glob:stag*");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getRealmMatchers()).hasSize(2);
	}

	@Test
	void shouldTrimRouteName() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("  test-route  ", config);

		// assert

		assertThat(result.getName()).isEqualTo("test-route");
	}

	@Test
	void shouldDefaultRealmMatchModeToDefault() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getRealmMatchMode()).isEqualTo(MatchMode.DEFAULT);
	}

	@Test
	void shouldDefaultEventMatchModeToDefault() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getEventMatchMode()).isEqualTo(MatchMode.DEFAULT);
	}

	@Test
	void shouldSetRealmMatchModeToAny() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("realm-match-mode", "any");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getRealmMatchMode()).isEqualTo(MatchMode.ANY);
	}

	@Test
	void shouldSetRealmMatchModeToAll() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("realm-match-mode", "all");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getRealmMatchMode()).isEqualTo(MatchMode.ALL);
	}

	@Test
	void shouldSetEventMatchModeToAny() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("event-match-mode", "any");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getEventMatchMode()).isEqualTo(MatchMode.ANY);
	}

	@Test
	void shouldSetEventMatchModeToAll() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("event-match-mode", "all");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getEventMatchMode()).isEqualTo(MatchMode.ALL);
	}

	@Test
	void shouldSetRealmMatchModeCaseInsensitive() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("realm-match-mode", "ALL");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getRealmMatchMode()).isEqualTo(MatchMode.ALL);
	}

	@Test
	void shouldThrowWhenRealmMatchModeIsInvalid() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("realm-match-mode", "invalid");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoute("test-route", config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("realm-match-mode must be 'any' or 'all'");
	}

	@Test
	void shouldThrowWhenEventMatchModeIsInvalid() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("event-match-mode", "invalid");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoute("test-route", config))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("event-match-mode must be 'any' or 'all'");
	}

	@Test
	void shouldCreateRouteWithNegatedMatcher() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("realm-matchers.nodev", "list:not:dev");
		map.put("serializer.kind", "json");
		map.put("destination.kind", "http");
		map.put("destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoute("test-route", config);

		// assert

		assertThat(result.getRealmMatchers()).hasSize(1);
		assertThat(result.acceptRealm("dev")).isFalse();
		assertThat(result.acceptRealm("master")).isTrue();
	}
}
