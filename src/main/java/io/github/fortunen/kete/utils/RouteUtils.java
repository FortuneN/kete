package io.github.fortunen.kete.utils;

import java.util.ArrayList;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.MatchMode;
import io.github.fortunen.kete.Route;
import lombok.SneakyThrows;

public final class RouteUtils {

	private RouteUtils() {}

	public static final String RETRY = "retry";
	public static final String ENABLED = "enabled";
	public static final String SERIALIZER = "serializer";
	public static final String DESTINATION = "destination";
	public static final String CONFIGURATION = "configuration";
	public static final String REALM_MATCHERS = "realm-matchers";
	public static final String EVENT_MATCHERS = "event-matchers";
	public static final String REALM_MATCH_MODE = "realm-match-mode";
	public static final String EVENT_MATCH_MODE = "event-match-mode";

	@SneakyThrows
	public static Route createRoute(String name, MapConfiguration configuration) {

		ValidationUtils.requireNonBlank(name, "name is required");
		ValidationUtils.requireNonNull(configuration, "configuration is required");

		if (!configuration.getBoolean(ENABLED, true)) {
			return null;
		}

		var route = new Route();

		// name

		route.setName(name.trim());

		// realm-matchers (optional - omission means all realms)
		// Format: realm-matchers.<name>=<kind>:(optional not:)<pattern>

		var realmMatchersConfig = ConfigurationUtils.getSubSet(configuration, REALM_MATCHERS);
		var realmMatcherKeys = ConfigurationUtils.getKeys(realmMatchersConfig);

		if (ValidationUtils.isNotNullOrEmpty(realmMatcherKeys)) {
			var realmMatchers = MatcherUtils.createMatchers(realmMatchersConfig);
			for (var matcher : realmMatchers) {
				matcher.initialize();
			}
			route.setRealmMatchers(realmMatchers);
		}

		// event-matchers (optional - omission means all events)
		// Format: event-matchers.<name>=<kind>:(optional not:)<pattern>

		var eventMatchersConfig = ConfigurationUtils.getSubSet(configuration, EVENT_MATCHERS);
		var eventMatcherKeys = ConfigurationUtils.getKeys(eventMatchersConfig);

		if (ValidationUtils.isNotNullOrEmpty(eventMatcherKeys)) {
			var eventMatchers = MatcherUtils.createMatchers(eventMatchersConfig);
			for (var matcher : eventMatchers) {
				matcher.initialize();
			}
			route.setEventMatchers(eventMatchers);
		}

		// realm-match-mode

		var realmMatchModeValue = configuration.getString(REALM_MATCH_MODE, "").trim();

		if (ValidationUtils.isNotBlank(realmMatchModeValue)) {

			var matchMode = ValidationUtils.tryParseEnum(MatchMode.class, realmMatchModeValue, true);

			ValidationUtils.requireTrue(matchMode.isPresent(), REALM_MATCH_MODE + " must be 'any' or 'all'");

			route.setRealmMatchMode(matchMode.get());
		}

		// event-match-mode

		var eventMatchModeValue = configuration.getString(EVENT_MATCH_MODE, "").trim();

		if (ValidationUtils.isNotBlank(eventMatchModeValue)) {

			var matchMode = ValidationUtils.tryParseEnum(MatchMode.class, eventMatchModeValue, true);

			ValidationUtils.requireTrue(matchMode.isPresent(), EVENT_MATCH_MODE + " must be 'any' or 'all'");

			route.setEventMatchMode(matchMode.get());
		}

		// serializer (defaults to json if not specified)

		var serializerConfig = ConfigurationUtils.getSubSet(configuration, SERIALIZER);
		var serializerKind = serializerConfig.getString(Constants.KIND, "").trim();

		if (ValidationUtils.isBlank(serializerKind)) {
			serializerConfig.getMap().put(Constants.KIND, "json");
		}

		var serializer = SerializerUtils.createSerializer(serializerConfig);

		route.setSerializer(serializer);

		// destinationConfig

		var destinationConfig = DestinationUtils.createDestinationConfig(ConfigurationUtils.getSubSet(configuration, DESTINATION));

		ValidationUtils.requireNonNull(destinationConfig, DESTINATION + " is required");

		route.setDestinationConfig(destinationConfig);

		// retry

		var retry = RetryUtils.createRetry(ConfigurationUtils.getSubSet(configuration, RETRY));

		if (ValidationUtils.isNotNull(retry)) {
			route.setRetry(retry);
		}

		// return

		return route;
	}

	public static Route[] createRoutes(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var routes = new ArrayList<Route>();

		for (var routeName : ConfigurationUtils.getFirstSegmentOfKeys(configuration)) {

			var route = createRoute(routeName, ConfigurationUtils.getSubSet(configuration, routeName));

			if (ValidationUtils.isNull(route)) {
				continue;
			}

			routes.add(route);
		}

		return routes.toArray(Route[]::new);
	}
}
