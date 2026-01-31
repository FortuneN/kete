package io.github.fortunen.kete.unittests.serializers.jsonserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

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
		adminEvent.setResourceType(org.keycloak.events.admin.ResourceType.USER);

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
		adminEvent.setResourceType(org.keycloak.events.admin.ResourceType.USER);
		adminEvent.setResourcePath("users/123");
		adminEvent.setAuthDetails(mock(org.keycloak.events.admin.AuthDetails.class));
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
		var details = new java.util.HashMap<String, String>();
		details.put("nested", "{\"level2\":{\"level3\":[\"a\",\"b\",\"c\"]}}");
		event.setDetails(details);

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 50).isTrue();
	}
}
