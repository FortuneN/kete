package io.github.fortunen.kete.unittests.serializers.jsonserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.fortunen.kete.serializers.JsonSerializer;

public class serializeTests {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void shouldSerializeEventToJson() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setUserId("test-user");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var parsed = MAPPER.readTree(result);
		assertThat(parsed.get("type").asText()).isEqualTo("LOGIN");
		assertThat(parsed.get("realmId").asText()).isEqualTo("test-realm");
		assertThat(parsed.get("userId").asText()).isEqualTo("test-user");
	}

	@Test
	public void shouldSerializeAdminEventToJson() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRealmId("test-realm");
		adminEvent.setResourceType(ResourceType.USER);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var parsed = MAPPER.readTree(result);
		assertThat(parsed.get("operationType").asText()).isEqualTo("CREATE");
		assertThat(parsed.get("realmId").asText()).isEqualTo("test-realm");
		assertThat(parsed.get("resourceType").asText()).isEqualTo("USER");
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new JsonSerializer();
		Event event = null;

		// act

		var thrown = catchThrowable(() -> serializer.serialize(event));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("event is required");
	}

	@Test
	public void shouldThrowWhenAdminEventIsNull() {

		// arrange

		var serializer = new JsonSerializer();
		AdminEvent adminEvent = null;

		// act

		var thrown = catchThrowable(() -> serializer.serialize(adminEvent));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("adminEvent is required");
	}

	@Test
	public void shouldSerializeEventWithNullFields() {

		// arrange

		var serializer = new JsonSerializer();
		var event = new Event();

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldSerializeAdminEventWithNullFields() {

		// arrange

		var serializer = new JsonSerializer();
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldSerializeEventWithAllFields() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var event = new Event();
		var currentTime = System.currentTimeMillis();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm");
		event.setUserId("user");
		event.setSessionId("session");
		event.setIpAddress("127.0.0.1");
		event.setClientId("client");
		event.setTime(currentTime);

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var parsed = MAPPER.readTree(result);
		assertThat(parsed.get("type").asText()).isEqualTo("LOGIN");
		assertThat(parsed.get("realmId").asText()).isEqualTo("realm");
		assertThat(parsed.get("userId").asText()).isEqualTo("user");
		assertThat(parsed.get("sessionId").asText()).isEqualTo("session");
		assertThat(parsed.get("ipAddress").asText()).isEqualTo("127.0.0.1");
		assertThat(parsed.get("clientId").asText()).isEqualTo("client");
		assertThat(parsed.get("time").asLong()).isEqualTo(currentTime);
	}

	@Test
	public void shouldSerializeAdminEventWithAllFields() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var adminEvent = new AdminEvent();
		var currentTime = System.currentTimeMillis();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRealmId("realm");
		adminEvent.setResourceType(ResourceType.USER);
		adminEvent.setResourcePath("users/123");
		adminEvent.setAuthDetails(mock(AuthDetails.class));
		adminEvent.setTime(currentTime);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var parsed = MAPPER.readTree(result);
		assertThat(parsed.get("operationType").asText()).isEqualTo("CREATE");
		assertThat(parsed.get("realmId").asText()).isEqualTo("realm");
		assertThat(parsed.get("resourceType").asText()).isEqualTo("USER");
		assertThat(parsed.get("resourcePath").asText()).isEqualTo("users/123");
		assertThat(parsed.get("time").asLong()).isEqualTo(currentTime);
	}

	@Test
	public void shouldReturnApplicationJsonContentType() {

		// arrange

		var serializer = new JsonSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/json");
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var event1 = new Event();
		event1.setType(EventType.LOGIN);
		var event2 = new Event();
		event2.setType(EventType.LOGOUT);

		// act

		var result1 = serializer.serialize(event1);
		var result2 = serializer.serialize(event2);

		// assert

		var parsed1 = MAPPER.readTree(result1);
		var parsed2 = MAPPER.readTree(result2);
		assertThat(parsed1.get("type").asText()).isEqualTo("LOGIN");
		assertThat(parsed2.get("type").asText()).isEqualTo("LOGOUT");
	}

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = new JsonSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test");

		// act

		var result1 = serializer.serialize(event);
		var result2 = serializer.serialize(event);

		// assert

		assertThat(result2).isEqualTo(result1);
	}

	@Test
	public void shouldHandleControlCharactersInStrings() {

		// arrange

		var serializer = new JsonSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setDetails(Map.of(
			"data", "line1\nline2\tcolumn\rreturn\u0000null"
		));

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		var json = new String(result);
		assertThat(json.contains("\\n") || json.contains("\\t")).isTrue();
	}

	@Test
	public void shouldHandleEventWithNullFields() {

		// arrange

		var serializer = new JsonSerializer();
		var event = new Event();
		// Don't set any fields - all null

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		var json = new String(result);
		assertThat(json.contains("null") || json.equals("{}")).isTrue();
	}

	@Test
	public void shouldSerializeNestedMapsAndLists() {

		// arrange

		var serializer = new JsonSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		var details = new HashMap<String, String>();
		details.put("nested", "{\"level2\":{\"level3\":[\"a\",\"b\",\"c\"]}}");
		event.setDetails(details);

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 50).isTrue();
	}

	// =====================================================================
	// JSON Schema conformance (schemas/json)
	// =====================================================================

	@Test
	public void shouldSerializeEventConformingToJsonSchema() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var schema = MAPPER.readTree(Files.readString(Path.of("schemas/json/event.json")));
		var event = new Event();
		event.setId("evt-001");
		event.setTime(1700000000000L);
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setRealmName("Test Realm");
		event.setClientId("my-client");
		event.setUserId("user-123");
		event.setSessionId("sess-456");
		event.setIpAddress("192.168.1.100");
		event.setError("auth_failed");
		event.setDetails(Map.of("username", "john.doe", "remember_me", "true"));

		// act

		var result = serializer.serialize(event);

		// assert

		var parsed = MAPPER.readTree(result);
		assertThat(violationsOf(parsed, schema, schema)).isEmpty();
	}

	@Test
	public void shouldSerializeEventWithNullFieldsConformingToJsonSchema() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var schema = MAPPER.readTree(Files.readString(Path.of("schemas/json/event.json")));
		var event = new Event();

		// act

		var result = serializer.serialize(event);

		// assert

		var parsed = MAPPER.readTree(result);
		assertThat(violationsOf(parsed, schema, schema)).isEmpty();
	}

	@Test
	public void shouldSerializeAdminEventConformingToJsonSchema() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var schema = MAPPER.readTree(Files.readString(Path.of("schemas/json/admin_event.json")));
		var authDetails = new AuthDetails();
		authDetails.setRealmId("auth-realm");
		authDetails.setRealmName("Auth Realm");
		authDetails.setClientId("admin-cli");
		authDetails.setUserId("admin-user");
		authDetails.setIpAddress("10.0.0.1");
		var adminEvent = new AdminEvent();
		adminEvent.setId("adm-001");
		adminEvent.setTime(1700000000000L);
		adminEvent.setRealmId("test-realm");
		adminEvent.setRealmName("Test Realm");
		adminEvent.setAuthDetails(authDetails);
		adminEvent.setResourceTypeAsString("USER");
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setResourcePath("users/user-123");
		adminEvent.setRepresentation("{\"username\":\"newuser\"}");
		adminEvent.setError("conflict");

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		var parsed = MAPPER.readTree(result);
		assertThat(violationsOf(parsed, schema, schema)).isEmpty();
	}

	@Test
	public void shouldSerializeAdminEventWithNullFieldsConformingToJsonSchema() throws Exception {

		// arrange

		var serializer = new JsonSerializer();
		var schema = MAPPER.readTree(Files.readString(Path.of("schemas/json/admin_event.json")));
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		var parsed = MAPPER.readTree(result);
		assertThat(violationsOf(parsed, schema, schema)).isEmpty();
	}

	private static List<String> violationsOf(JsonNode value, JsonNode schema, JsonNode root) {

		var violations = new ArrayList<String>();

		if (schema.has("$ref")) {
			return violationsOf(value, root.at(schema.get("$ref").asText().substring(1)), root);
		}

		if (schema.has("oneOf")) {
			var matchingBranches = 0;
			for (var branch : schema.get("oneOf")) {
				if (violationsOf(value, branch, root).isEmpty()) {
					matchingBranches++;
				}
			}
			if (matchingBranches != 1) {
				violations.add(value + " matches " + matchingBranches + " oneOf branches, expected exactly 1");
			}
			return violations;
		}

		var allowedTypes = new ArrayList<String>();
		if (schema.get("type").isArray()) {
			schema.get("type").forEach(type -> allowedTypes.add(type.asText()));
		} else {
			allowedTypes.add(schema.get("type").asText());
		}
		if (!allowedTypes.contains(jsonTypeOf(value))) {
			violations.add(value + " is " + jsonTypeOf(value) + ", expected one of " + allowedTypes);
		}

		if (value.isObject() && schema.has("properties")) {
			for (var required : schema.get("required")) {
				if (!value.has(required.asText())) {
					violations.add("required property '" + required.asText() + "' is missing");
				}
			}
			for (var property : value.properties()) {
				if (!schema.get("properties").has(property.getKey())) {
					violations.add("property '" + property.getKey() + "' is not described by the schema");
				} else {
					violations.addAll(violationsOf(property.getValue(), schema.get("properties").get(property.getKey()), root));
				}
			}
		}

		if (value.isObject() && schema.has("additionalProperties") && schema.get("additionalProperties").isObject()) {
			value.forEach(entry -> violations.addAll(violationsOf(entry, schema.get("additionalProperties"), root)));
		}

		return violations;
	}

	private static String jsonTypeOf(JsonNode value) {

		if (value.isNull()) {
			return "null";
		}
		if (value.isTextual()) {
			return "string";
		}
		if (value.isIntegralNumber()) {
			return "integer";
		}
		if (value.isNumber()) {
			return "number";
		}
		if (value.isBoolean()) {
			return "boolean";
		}
		if (value.isArray()) {
			return "array";
		}
		return "object";
	}
}
