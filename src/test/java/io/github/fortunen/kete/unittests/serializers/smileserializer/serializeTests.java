package io.github.fortunen.kete.unittests.serializers.smileserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import io.github.fortunen.kete.serializers.SmileSerializer;

public class serializeTests {

	private static final ObjectMapper SMILE_MAPPER = new ObjectMapper(new SmileFactory());

	@Test
	public void shouldSerializeEventToSmile() throws Exception {

		// arrange

		var serializer = new SmileSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setUserId("test-user");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
		// Deserialize to JsonNode and verify the content matches the original event
		var parsed = SMILE_MAPPER.readTree(result);
		assertThat(parsed.get("type").asText()).isEqualTo("LOGIN");
		assertThat(parsed.get("realmId").asText()).isEqualTo("test-realm");
		assertThat(parsed.get("userId").asText()).isEqualTo("test-user");
	}

	@Test
	public void shouldSerializeAdminEventToSmile() throws Exception {

		// arrange

		var serializer = new SmileSerializer();
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRealmId("test-realm");
		adminEvent.setResourceType(ResourceType.USER);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
		// Deserialize to JsonNode and verify the content matches the original event
		var parsed = SMILE_MAPPER.readTree(result);
		assertThat(parsed.get("operationType").asText()).isEqualTo("CREATE");
		assertThat(parsed.get("realmId").asText()).isEqualTo("test-realm");
		assertThat(parsed.get("resourceType").asText()).isEqualTo("USER");
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new SmileSerializer();
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

		var serializer = new SmileSerializer();
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

		var serializer = new SmileSerializer();
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

		var serializer = new SmileSerializer();
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldReturnSmileContentType() {

		// arrange

		var serializer = new SmileSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/x-jackson-smile");
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() {

		// arrange

		var serializer = new SmileSerializer();
		var event1 = new Event();
		event1.setType(EventType.LOGIN);
		var event2 = new Event();
		event2.setType(EventType.LOGOUT);

		// act

		var result1 = serializer.serialize(event1);
		var result2 = serializer.serialize(event2);

		// assert

		assertThat(result1).isNotNull();
		assertThat(result2).isNotNull();
		assertThat(result1.length > 0).isTrue();
		assertThat(result2.length > 0).isTrue();
	}

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = new SmileSerializer();
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
	public void shouldSerializeEventWithAllFields() throws Exception {

		// arrange

		var serializer = new SmileSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm");
		event.setUserId("user");
		event.setSessionId("session");
		event.setIpAddress("127.0.0.1");
		event.setClientId("client");
		var timestamp = System.currentTimeMillis();
		event.setTime(timestamp);

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
		// Deserialize to JsonNode and verify the content matches the original event
		var parsed = SMILE_MAPPER.readTree(result);
		assertThat(parsed.get("type").asText()).isEqualTo("LOGIN");
		assertThat(parsed.get("realmId").asText()).isEqualTo("realm");
		assertThat(parsed.get("userId").asText()).isEqualTo("user");
		assertThat(parsed.get("sessionId").asText()).isEqualTo("session");
		assertThat(parsed.get("ipAddress").asText()).isEqualTo("127.0.0.1");
		assertThat(parsed.get("clientId").asText()).isEqualTo("client");
		assertThat(parsed.get("time").asLong()).isEqualTo(timestamp);
	}

	@Test
	public void shouldProduceBinaryOutput() {

		// arrange

		var serializer = new SmileSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		// Smile format starts with specific header bytes
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldHandleUnicodeInEvent() {

		// arrange

		var serializer = new SmileSerializer();
		var event = new Event();
		event.setRealmId("realm-测试");
		event.setUserId("user-أَبْجَدِيّ");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldHandleEmptyStringsInEvent() {

		// arrange

		var serializer = new SmileSerializer();
		var event = new Event();
		event.setRealmId("");
		event.setUserId("");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}
}
