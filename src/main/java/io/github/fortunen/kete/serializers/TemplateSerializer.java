package io.github.fortunen.kete.serializers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.utils.Base64Utils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "template", scope = Component.TRANSIENT)
public class TemplateSerializer extends Serializer {

	private static final String DEFAULT_ESCAPE = "none";
	private static final String CONTENT_TYPE_KEY = "content-type";
	private static final String VARIABLE_ESCAPE = "variable-escape";
	private static final String DEFAULT_CONTENT_TYPE = "text/plain";
	private static final String TEMPLATE_FILE_TEXT = "template-file-text";
	private static final String TEMPLATE_FILE_PATH = "template-file-path";
	private static final String TEMPLATE_FILE_BASE64 = "template-file-base64";

	private String template;
	private String variableEscape;

	@Override
	@SneakyThrows
	public void initialize() {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		contentType = configuration.getString(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE).trim();
		variableEscape = configuration.getString(VARIABLE_ESCAPE, DEFAULT_ESCAPE).trim().toLowerCase();

		// triple-source template loading (exactly one must be set)

		var text = configuration.getString(TEMPLATE_FILE_TEXT, null);
		var base64 = configuration.getString(TEMPLATE_FILE_BASE64, null);
		var path = configuration.getString(TEMPLATE_FILE_PATH, null);

		var sourceCount = (ValidationUtils.isNotBlank(text) ? 1 : 0)
			+ (ValidationUtils.isNotBlank(base64) ? 1 : 0)
			+ (ValidationUtils.isNotBlank(path) ? 1 : 0);

		ValidationUtils.requireTrue(sourceCount == 1, "exactly one of " + TEMPLATE_FILE_TEXT + ", " + TEMPLATE_FILE_BASE64 + ", or " + TEMPLATE_FILE_PATH + " must be set");

		if (ValidationUtils.isNotBlank(text)) {
			template = text;
		} else if (ValidationUtils.isNotBlank(base64)) {
			template = new String(Base64Utils.decode(base64.trim()), StandardCharsets.UTF_8);
		} else {
			template = Files.readString(Paths.get(path.trim()));
		}

		ValidationUtils.requireNonBlank(template, "template content must not be blank");
	}

	@Override
	public byte[] serialize(Event event) {

		ValidationUtils.requireNonNull(event, "event is required");

		var values = buildEventValues(event);
		var resolved = resolve(values);

		return resolved.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public byte[] serialize(AdminEvent adminEvent) {

		ValidationUtils.requireNonNull(adminEvent, "adminEvent is required");

		var values = buildAdminEventValues(adminEvent);
		var resolved = resolve(values);

		return resolved.getBytes(StandardCharsets.UTF_8);
	}

	private Map<String, String> buildEventValues(Event event) {

		var values = new HashMap<String, String>();

		putIfNotNull(values, "id", event.getId());
		putIfNotNull(values, "type", ValidationUtils.isNotNull(event.getType()) ? event.getType().name() : null);
		putIfNotNull(values, "realmId", event.getRealmId());
		putIfNotNull(values, "realmName", event.getRealmName());
		putIfNotNull(values, "clientId", event.getClientId());
		putIfNotNull(values, "userId", event.getUserId());
		putIfNotNull(values, "sessionId", event.getSessionId());
		putIfNotNull(values, "ipAddress", event.getIpAddress());
		putIfNotNull(values, "error", event.getError());
		putIfNotNull(values, "time", String.valueOf(event.getTime()));

		// details.* (dot-path traversal, with camelCase aliases for snake_case keys)

		if (ValidationUtils.isNotNull(event.getDetails())) {
			for (var entry : event.getDetails().entrySet()) {
				putIfNotNull(values, "details." + entry.getKey(), entry.getValue());

				var camelKey = snakeToCamelCase(entry.getKey());

				if (!camelKey.equals(entry.getKey())) {
					putIfNotNull(values, "details." + camelKey, entry.getValue());
				}
			}
		}

		return values;
	}

	private Map<String, String> buildAdminEventValues(AdminEvent adminEvent) {

		var values = new HashMap<String, String>();

		putIfNotNull(values, "id", adminEvent.getId());
		putIfNotNull(values, "operationType", ValidationUtils.isNotNull(adminEvent.getOperationType()) ? adminEvent.getOperationType().name() : null);
		putIfNotNull(values, "resourceType", adminEvent.getResourceTypeAsString());
		putIfNotNull(values, "realmId", adminEvent.getRealmId());
		putIfNotNull(values, "realmName", adminEvent.getRealmName());
		putIfNotNull(values, "resourcePath", adminEvent.getResourcePath());
		putIfNotNull(values, "representation", adminEvent.getRepresentation());
		putIfNotNull(values, "error", adminEvent.getError());
		putIfNotNull(values, "time", String.valueOf(adminEvent.getTime()));

		// authDetails.*

		if (ValidationUtils.isNotNull(adminEvent.getAuthDetails())) {
			var auth = adminEvent.getAuthDetails();
			putIfNotNull(values, "authDetails.realmId", auth.getRealmId());
			putIfNotNull(values, "authDetails.realmName", auth.getRealmName());
			putIfNotNull(values, "authDetails.clientId", auth.getClientId());
			putIfNotNull(values, "authDetails.userId", auth.getUserId());
			putIfNotNull(values, "authDetails.ipAddress", auth.getIpAddress());
		}

		return values;
	}

	private String resolve(Map<String, String> values) {

		// apply global escape to all values (unless per-variable override via :filter)

		var escapedValues = new HashMap<String, String>();

		for (var entry : values.entrySet()) {
			escapedValues.put(entry.getKey(), escape(entry.getValue(), variableEscape));
		}

		// resolve variables with :filter suffix (per-variable override)

		var substitutor = new StringSubstitutor(key -> {

			// check for per-variable filter: ${variable:filter}

			var colonIndex = key.indexOf(':');

			if (colonIndex > 0) {
				var variableName = key.substring(0, colonIndex);
				var filter = key.substring(colonIndex + 1).trim().toLowerCase();
				var raw = values.get(variableName);
				return ValidationUtils.isNotNull(raw) ? escape(raw, filter) : "";
			}

			// use pre-escaped value (global escape applied)

			var escaped = escapedValues.get(key);
			return ValidationUtils.isNotNull(escaped) ? escaped : "";

		});

		substitutor.setEnableUndefinedVariableException(false);

		return substitutor.replace(template);
	}

	public static String escape(String value, String filter) {

		if (ValidationUtils.isNull(value)) {
			return "";
		}

		return switch (filter) {
			case "none" -> value;
			case "csv", "csv-quote" -> escapeCsv(value);
			case "xml", "xml-encode" -> escapeXml(value);
			case "json", "json-encode" -> escapeJson(value);
			case "url", "url-encode" -> URLEncoder.encode(value, StandardCharsets.UTF_8);
			case "b64", "base64" -> Base64Utils.encode(value.getBytes(StandardCharsets.UTF_8));
			default -> value;
		};
	}

	public static String escapeJson(String value) {

		var sb = new StringBuilder(value.length() + 16);

		for (var i = 0; i < value.length(); i++) {

			var c = value.charAt(i);

			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				case '\b' -> sb.append("\\b");
				case '\f' -> sb.append("\\f");
				default -> {
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
				}
			}
		}

		return sb.toString();
	}

	public static String escapeXml(String value) {

		var sb = new StringBuilder(value.length() + 16);

		for (var i = 0; i < value.length(); i++) {

			var c = value.charAt(i);

			switch (c) {
				case '&' -> sb.append("&amp;");
				case '<' -> sb.append("&lt;");
				case '>' -> sb.append("&gt;");
				case '"' -> sb.append("&quot;");
				case '\'' -> sb.append("&apos;");
				default -> sb.append(c);
			}
		}

		return sb.toString();
	}

	public static String escapeCsv(String value) {

		if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}

		return value;
	}

	private static void putIfNotNull(Map<String, String> map, String key, String value) {
		if (ValidationUtils.isNotNull(value)) {
			map.put(key, value);
		}
	}

	public static String snakeToCamelCase(String key) {

		if (ValidationUtils.isNull(key) || !key.contains("_")) {
			return key;
		}

		var parts = key.split("_");
		var sb = new StringBuilder(parts[0]);

		for (var i = 1; i < parts.length; i++) {
			if (!parts[i].isEmpty()) {
				sb.append(Character.toUpperCase(parts[i].charAt(0)));
				sb.append(parts[i].substring(1));
			}
		}

		return sb.toString();
	}
}
