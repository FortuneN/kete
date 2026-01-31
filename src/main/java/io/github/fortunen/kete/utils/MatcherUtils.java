package io.github.fortunen.kete.utils;

import java.util.ArrayList;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.StringUtils;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.MatchMode;
import io.github.fortunen.kete.Matcher;
import lombok.SneakyThrows;

public final class MatcherUtils {

	private MatcherUtils() {}

	private static final String NOT = "not";
	private static final String SEPARATOR = ":";

	@SneakyThrows
	public static Matcher createMatcher(String name, String value) {

		ValidationUtils.requireNonBlank(name, "name is required");
		ValidationUtils.requireNonBlank(value, "matcher value is required");

		var parts = StringUtils.splitPreserveAllTokens(value.trim(), SEPARATOR, 3);

		ValidationUtils.requireTrue(parts.length >= 2, "matcher must be in format '" + Constants.KIND + ":pattern' or '" + Constants.KIND + ":not:pattern'");

		var kind = ValidationUtils.requireNonBlank(parts[0].trim(), Constants.KIND + " is required");

		var not = false;
		String pattern;

		if (parts.length == 3 && NOT.equalsIgnoreCase(parts[1].trim())) {

			// Format: kind:not:pattern

			not = true;
			pattern = ValidationUtils.requireNonBlank(parts[2].trim(), "pattern is required");

		} else if (parts.length == 2) {

			// Format: kind:pattern

			pattern = ValidationUtils.requireNonBlank(parts[1].trim(), "pattern is required");

		} else {

			// Format: kind:something:rest where something != "not"
			// Treat as kind:pattern where pattern contains colons

			pattern = ValidationUtils.requireNonBlank(parts[1].trim() + SEPARATOR + parts[2].trim(), "pattern is required");
		}

		var matcher = ValidationUtils.requireNonNull(IocUtils.get(kind, Matcher.class), "matcher " + Constants.KIND + " '" + kind + "' not found");

		matcher.setName(name.trim());
		matcher.setPattern(pattern);
		matcher.setNot(not);

		return matcher;
	}

	public static Matcher[] createMatchers(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var matchers = new ArrayList<Matcher>();

		for (var matcherName : ConfigurationUtils.getKeys(configuration)) {

			var value = configuration.getString(matcherName, "");
			var matcher = createMatcher(matcherName, value);

			matchers.add(matcher);
		}

		return matchers.toArray(Matcher[]::new);
	}

	public static boolean matchWithMode(String value, Matcher[] matchers, MatchMode mode) {

		ValidationUtils.requireNonNull(value, "value is required");
		ValidationUtils.requireNonNull(matchers, "matchers is required");
		ValidationUtils.requireNonNull(mode, "mode is required");

		if (mode == MatchMode.ALL) {

			for (var matcher : matchers) {
				if (!matcher.accept(value)) {
					return false;
				}
			}

			return true;

		} else {

			for (var matcher : matchers) {
				if (matcher.accept(value)) {
					return true;
				}
			}

			return false;
		}
	}
}
