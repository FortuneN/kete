package io.github.fortunen.kete.unittests.serializers.yamlserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.github.fortunen.kete.serializers.YamlSerializer;

public class serializeTests {

	private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

	@Test
	public void shouldSerializeEventToYaml() throws Exception {

		// arrange

		var serializer = new YamlSerializer();
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
	public void shouldSerializeAdminEventToYaml() throws Exception {

		// arrange

		var serializer = new YamlSerializer();
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

		var serializer = new YamlSerializer();
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

		var serializer = new YamlSerializer();
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

		var serializer = new YamlSerializer();
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

		var serializer = new YamlSerializer();
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldReturnApplicationYamlContentType() {

		// arrange

		var serializer = new YamlSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/yaml");
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() {

		// arrange

		var serializer = new YamlSerializer();
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
		var yaml1 = new String(result1);
		var yaml2 = new String(result2);
		assertThat(yaml1.contains("LOGIN")).isTrue();
		assertThat(yaml2.contains("LOGOUT")).isTrue();
	}

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = new YamlSerializer();
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

		var serializer = new YamlSerializer();
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
		var parsed = MAPPER.readTree(result);
		assertThat(parsed.get("type").asText()).isEqualTo("LOGIN");
		assertThat(parsed.get("realmId").asText()).isEqualTo("realm");
		assertThat(parsed.get("userId").asText()).isEqualTo("user");
		assertThat(parsed.get("sessionId").asText()).isEqualTo("session");
		assertThat(parsed.get("ipAddress").asText()).isEqualTo("127.0.0.1");
		assertThat(parsed.get("clientId").asText()).isEqualTo("client");
		assertThat(parsed.get("time").asLong()).isEqualTo(timestamp);
	}

	@Test
	public void shouldSerializeWithIndentation() {

		// arrange

		var serializer = new YamlSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);

		// act

		var result = serializer.serialize(event);

		// assert

		var yaml = new String(result);
		// YAML should have newlines for structure
		assertThat(yaml.contains("\n")).isTrue();
	}
}
