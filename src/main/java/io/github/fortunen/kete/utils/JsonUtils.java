package io.github.fortunen.kete.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.SneakyThrows;

public final class JsonUtils {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final ObjectMapper COPY_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private JsonUtils() {}

	@SneakyThrows
	public static JsonNode parseJson(String json) {

		ValidationUtils.requireNonNull(json, "json is required");

		return MAPPER.readTree(json);
	}

	public static <T> T copy(T value, Class<T> type) {

		ValidationUtils.requireNonNull(value, "value is required");
		ValidationUtils.requireNonNull(type, "type is required");

		return COPY_MAPPER.convertValue(value, type);
	}

	public static ObjectNode createObjectNode() {

		return MAPPER.createObjectNode();
	}

	public static String getString(JsonNode node, String field) {

		ValidationUtils.requireNonNull(node, "node is required");
		ValidationUtils.requireNonBlank(field, "field is required");

		var value = node.get(field);

		return ValidationUtils.isNotNull(value) && !value.isNull() ? value.asText() : null;
	}

	public static Long getLong(JsonNode node, String field) {

		ValidationUtils.requireNonNull(node, "node is required");
		ValidationUtils.requireNonBlank(field, "field is required");

		var value = node.get(field);

		return ValidationUtils.isNotNull(value) && !value.isNull() ? value.asLong() : null;
	}
}
