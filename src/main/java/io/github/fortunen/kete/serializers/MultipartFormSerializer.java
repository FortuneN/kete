package io.github.fortunen.kete.serializers;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "multipart-form", scope = Component.TRANSIENT)
public class MultipartFormSerializer extends Serializer {

	private static final String NESTING_NOTATION_KEY = "nesting-notation";
	private static final String DEFAULT_NESTING_NOTATION = "bracket";
	private static final String BOUNDARY = "kete-boundary";
	private static final String CRLF = "\r\n";

	private String contentType = "multipart/form-data; boundary=" + BOUNDARY;
	private String nestingNotation;

	@Override
	public void initialize() {

		nestingNotation = ValidationUtils.isNotNull(configuration)
			? configuration.getString(NESTING_NOTATION_KEY, DEFAULT_NESTING_NOTATION).trim().toLowerCase()
			: DEFAULT_NESTING_NOTATION;

		ValidationUtils.requireTrue(
			"bracket".equals(nestingNotation) || "dot".equals(nestingNotation),
			"nesting-notation must be 'bracket' or 'dot', got: " + nestingNotation
		);
	}

	@Override
	public byte[] serialize(Event event) {

		ValidationUtils.requireNonNull(event, "event is required");

		var pairs = buildEventPairs(event);
		return buildMultipart(pairs).getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public byte[] serialize(AdminEvent adminEvent) {

		ValidationUtils.requireNonNull(adminEvent, "adminEvent is required");

		var pairs = buildAdminEventPairs(adminEvent);
		return buildMultipart(pairs).getBytes(StandardCharsets.UTF_8);
	}

	private Map<String, String> buildEventPairs(Event event) {

		var pairs = new LinkedHashMap<String, String>();
		putIfNotNull(pairs, "id", event.getId());
		putIfNotNull(pairs, "type", ValidationUtils.isNotNull(event.getType()) ? event.getType().name() : null);
		putIfNotNull(pairs, "realmId", event.getRealmId());
		putIfNotNull(pairs, "realmName", event.getRealmName());
		putIfNotNull(pairs, "clientId", event.getClientId());
		putIfNotNull(pairs, "userId", event.getUserId());
		putIfNotNull(pairs, "sessionId", event.getSessionId());
		putIfNotNull(pairs, "ipAddress", event.getIpAddress());
		putIfNotNull(pairs, "error", event.getError());
		putIfNotNull(pairs, "time", String.valueOf(event.getTime()));

		if (ValidationUtils.isNotNull(event.getDetails())) {
			for (var entry : event.getDetails().entrySet()) {
				putIfNotNull(pairs, nestedKey("details", entry.getKey()), entry.getValue());
			}
		}

		return pairs;
	}

	private Map<String, String> buildAdminEventPairs(AdminEvent adminEvent) {

		var pairs = new LinkedHashMap<String, String>();
		putIfNotNull(pairs, "id", adminEvent.getId());
		putIfNotNull(pairs, "operationType", ValidationUtils.isNotNull(adminEvent.getOperationType()) ? adminEvent.getOperationType().name() : null);
		putIfNotNull(pairs, "resourceType", adminEvent.getResourceTypeAsString());
		putIfNotNull(pairs, "realmId", adminEvent.getRealmId());
		putIfNotNull(pairs, "realmName", adminEvent.getRealmName());
		putIfNotNull(pairs, "resourcePath", adminEvent.getResourcePath());
		putIfNotNull(pairs, "representation", adminEvent.getRepresentation());
		putIfNotNull(pairs, "error", adminEvent.getError());
		putIfNotNull(pairs, "time", String.valueOf(adminEvent.getTime()));

		if (ValidationUtils.isNotNull(adminEvent.getAuthDetails())) {
			var auth = adminEvent.getAuthDetails();
			putIfNotNull(pairs, nestedKey("authDetails", "realmId"), auth.getRealmId());
			putIfNotNull(pairs, nestedKey("authDetails", "realmName"), auth.getRealmName());
			putIfNotNull(pairs, nestedKey("authDetails", "clientId"), auth.getClientId());
			putIfNotNull(pairs, nestedKey("authDetails", "userId"), auth.getUserId());
			putIfNotNull(pairs, nestedKey("authDetails", "ipAddress"), auth.getIpAddress());
		}

		return pairs;
	}

	private String nestedKey(String parent, String child) {

		return "bracket".equals(nestingNotation)
			? parent + "[" + child + "]"
			: parent + "." + child;
	}

	private String buildMultipart(Map<String, String> pairs) {

		var sb = new StringBuilder();

		for (var entry : pairs.entrySet()) {
			sb.append("--").append(BOUNDARY).append(CRLF);
			sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(CRLF);
			sb.append(CRLF);
			sb.append(entry.getValue()).append(CRLF);
		}

		sb.append("--").append(BOUNDARY).append("--").append(CRLF);

		return sb.toString();
	}

	private void putIfNotNull(Map<String, String> map, String key, String value) {

		if (ValidationUtils.isNotNull(value)) {
			map.put(key, value);
		}
	}
}
