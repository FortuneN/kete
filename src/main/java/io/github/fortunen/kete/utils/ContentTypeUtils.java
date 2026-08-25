package io.github.fortunen.kete.utils;

import java.util.Locale;

public final class ContentTypeUtils {

	private ContentTypeUtils() {}

	private static final String[] TEXT_MARKERS = { "json", "xml", "yaml", "csv", "toml", "properties", "x-www-form-urlencoded", "javascript" };

	// true for content types whose bodies are character data and can be carried by text-only transports

	public static boolean isText(String contentType) {

		if (ValidationUtils.isBlank(contentType)) {
			return false;
		}

		var normalized = contentType.trim().toLowerCase(Locale.ROOT);

		if (normalized.startsWith("text/")) {
			return true;
		}

		for (var marker : TEXT_MARKERS) {
			if (normalized.contains(marker)) {
				return true;
			}
		}

		return false;
	}
}
