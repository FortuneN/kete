package io.github.fortunen.kete.unittests.utils.routeutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.RouteUtils;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

class createRoutesTests {

	@Test
	void shouldThrowWhenConfigurationIsNull() {

		// act & assert

		assertThatThrownBy(() -> RouteUtils.createRoutes(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("configuration is required");
	}

	@Test
	void shouldReturnEmptyArrayForEmptyConfiguration() {

		// arrange

		var config = new MapConfiguration(new HashMap<>());

		// act

		var result = RouteUtils.createRoutes(config);

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	void shouldCreateSingleRoute() {

		// arrange
		// Routes can omit realm-matchers (means all realms)

		var map = new HashMap<String, Object>();
		map.put("route1.realm-matchers.prod", "list:master");
		map.put("route1.serializer.kind", "json");
		map.put("route1.destination.kind", "http");
		map.put("route1.destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoutes(config);

		// assert

		assertThat(result).hasSize(1);
		assertThat(result[0].getName()).isEqualTo("route1");
		assertThat(result[0].getRealmMatchers()).hasSize(1);
		assertThat(result[0].acceptRealm("master")).isTrue();
	}

	@Test
	void shouldCreateRouteWithoutRealmMatchers() {

		// arrange
		// Route without realm-matchers accepts all realms

		var map = new HashMap<String, Object>();
		map.put("route1.serializer.kind", "json");
		map.put("route1.destination.kind", "http");
		map.put("route1.destination.url", "http://localhost:8080");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoutes(config);

		// assert

		assertThat(result).hasSize(1);
		assertThat(result[0].getName()).isEqualTo("route1");
		assertThat(result[0].getRealmMatchers()).isEmpty();
		assertThat(result[0].acceptRealm("any-realm")).isTrue();
	}

	@Test
	void shouldCreateMultipleRoutes() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("route1.realm-matchers.prod", "list:realm1");
		map.put("route1.serializer.kind", "json");
		map.put("route1.destination.kind", "http");
		map.put("route1.destination.url", "http://localhost:8080");
		map.put("route2.realm-matchers.prod", "list:realm2");
		map.put("route2.serializer.kind", "xml");
		map.put("route2.destination.kind", "http");
		map.put("route2.destination.url", "http://localhost:8081");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoutes(config);

		// assert

		assertThat(result).hasSize(2);
		var names = Arrays.stream(result).map(r -> r.getName()).toList();
		assertThat(names).containsExactlyInAnyOrder("route1", "route2");
	}

	@Test
	void shouldSkipDisabledRoutes() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("route1.realm-matchers.prod", "list:realm1");
		map.put("route1.serializer.kind", "json");
		map.put("route1.destination.kind", "http");
		map.put("route1.destination.url", "http://localhost:8080");
		map.put("route2.enabled", "false");
		map.put("route2.realm-matchers.prod", "list:realm2");
		map.put("route2.serializer.kind", "json");
		map.put("route2.destination.kind", "http");
		map.put("route2.destination.url", "http://localhost:8081");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoutes(config);

		// assert

		assertThat(result).hasSize(1);
		assertThat(result[0].getName()).isEqualTo("route1");
	}

	@Test
	void shouldCreateRoutesWithDifferentSerializersAndDestinations() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("httpjson.realm-matchers.prod", "list:realm1");
		map.put("httpjson.serializer.kind", "json");
		map.put("httpjson.destination.kind", "http");
		map.put("httpjson.destination.url", "http://localhost:8080");
		map.put("kafkaxml.realm-matchers.prod", "list:realm2");
		map.put("kafkaxml.serializer.kind", "xml");
		map.put("kafkaxml.destination.kind", "kafka");
		map.put("kafkaxml.destination.bootstrap-servers", "localhost:9092");
		map.put("kafkaxml.destination.topic", "test-topic");
		var config = new MapConfiguration(map);

		// act

		var result = RouteUtils.createRoutes(config);

		// assert

		assertThat(result).hasSize(2);
	}
}
