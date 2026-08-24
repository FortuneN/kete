package io.github.fortunen.kete.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.Config.Scope;

import io.github.fortunen.kete.Configuration;
import io.github.fortunen.kete.Constants;

public final class ConfigurationUtils {

	private ConfigurationUtils() {}

	public static final String ROUTES = "routes";
	public static final String LOGGING = "logging";
	public static final String ENABLED = "enabled";
	public static final String METRICS = "metrics";
	public static final String SUPPORT_THE_PROJECT_MESSAGE = "support-the-project-message";

	public static MapConfiguration getSubSet(MapConfiguration configuration, String prefix) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");
		ValidationUtils.requireNonNull(prefix, "prefix is required");

		var trimmed = new HashMap<String, Object>();

		configuration.subset(prefix).entrySet().forEach(entry -> {
			trimmed.put(entry.getKey().trim(), ValidationUtils.requireNonNullElse(entry.getValue(), "").toString().trim());
		});

		return new MapConfiguration(trimmed);
	}

	public static String[] getKeys(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var set = new HashSet<String>();

		configuration.getKeys().forEachRemaining(key -> {
			set.add(key.trim());
		});

		return set.toArray(String[]::new);
	}

	public static String[] getFirstSegmentOfKeys(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var set = new HashSet<String>();

		configuration.getKeys().forEachRemaining(key -> {
			set.add(StringUtils.split(key, "." , 2)[0].trim());
		});

		return set.toArray(String[]::new);
	}

	public static Configuration createConfiguration(Scope scope, Map<String, String> environment) {

		ValidationUtils.requireNonNull(scope, "scope is required");
		ValidationUtils.requireNonNull(environment, "environment is required");

		var merged = new HashMap<String, Object>();

		// from scope

		for (var key : scope.getPropertyNames()) {
			merged.put(key, scope.get(key));
		}

		// from envars (overwrite)

		environment.forEach((key, value) -> {
			merged.put(key, value);
		});

		// config

		var map = new MapConfiguration(merged);
		var configValues = ConfigurationUtils.getSubSet(map, Constants.ID);

		if (!configValues.getBoolean(ENABLED, true)) {
			return null;
		}

		var config = new Configuration();

		// metrics

		var metricsSubset = ConfigurationUtils.getSubSet(configValues, METRICS);
		config.setMetricsEnabled(metricsSubset.getBoolean(ENABLED, false));

		// support-the-project-message

		config.setSupportTheProject(configValues.getBoolean(SUPPORT_THE_PROJECT_MESSAGE, true));

		// routes

		var routesSubset = ConfigurationUtils.getSubSet(configValues, ROUTES);
		var routes = RouteUtils.createRoutes(routesSubset);

		if (ValidationUtils.isNotNull(routes)) {
			config.setRoutes(routes);
		}

		// return

		return config;
	}
}
