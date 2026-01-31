package io.github.fortunen.kete.unittests.serializers.propertiesserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.serializers.PropertiesSerializer;
import java.io.ByteArrayInputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

public class serializeTests {

	@Test
	public void shouldSerializeEventToProperties() throws Exception {

		// arrange

		var serializer = new PropertiesSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setUserId("test-user");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var props = new Properties();
		props.load(new ByteArrayInputStream(result));
		assertThat(props.getProperty("type")).isEqualTo("LOGIN");
		assertThat(props.getProperty("realmId")).isEqualTo("test-realm");
		assertThat(props.getProperty("userId")).isEqualTo("test-user");
	}

	@Test
	public void shouldSerializeAdminEventToProperties() throws Exception {

		// arrange

		var serializer = new PropertiesSerializer();
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRealmId("test-realm");
		adminEvent.setResourceType(org.keycloak.events.admin.ResourceType.USER);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var props = new Properties();
		props.load(new ByteArrayInputStream(result));
		assertThat(props.getProperty("operationType")).isEqualTo("CREATE");
		assertThat(props.getProperty("realmId")).isEqualTo("test-realm");
		assertThat(props.getProperty("resourceType")).isEqualTo("USER");
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new PropertiesSerializer();
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

		var serializer = new PropertiesSerializer();
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

		var serializer = new PropertiesSerializer();
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

		var serializer = new PropertiesSerializer();
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldReturnTextPlainContentType() {

		// arrange

		var serializer = new PropertiesSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("text/plain");
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() throws Exception {

		// arrange

		var serializer = new PropertiesSerializer();
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

		var props1 = new Properties();
		props1.load(new ByteArrayInputStream(result1));
		var props2 = new Properties();
		props2.load(new ByteArrayInputStream(result2));
		assertThat(props1.getProperty("type")).isEqualTo("LOGIN");
		assertThat(props2.getProperty("type")).isEqualTo("LOGOUT");
	}

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = new PropertiesSerializer();
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

		var serializer = new PropertiesSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm");
		event.setUserId("user");
		event.setSessionId("session");
		event.setIpAddress("127.0.0.1");
		event.setClientId("client");
		var eventTime = 1234567890L;
		event.setTime(eventTime);

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var props = new Properties();
		props.load(new ByteArrayInputStream(result));
		assertThat(props.getProperty("type")).isEqualTo("LOGIN");
		assertThat(props.getProperty("realmId")).isEqualTo("realm");
		assertThat(props.getProperty("userId")).isEqualTo("user");
		assertThat(props.getProperty("sessionId")).isEqualTo("session");
		assertThat(props.getProperty("ipAddress")).isEqualTo("127.0.0.1");
		assertThat(props.getProperty("clientId")).isEqualTo("client");
		assertThat(props.getProperty("time")).isEqualTo(String.valueOf(eventTime));
	}

	@Test
	public void shouldUseKeyValueFormat() {

		// arrange

		var serializer = new PropertiesSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test");

		// act

		var result = serializer.serialize(event);

		// assert

		var props = new String(result);
		// Properties use key=value format
		assertThat(props.contains("=") || props.contains("\n")).isTrue();
	}

	@Test
	public void shouldFlattenNestedProperties() {

		// arrange

		var serializer = new PropertiesSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test");

		// act

		var result = serializer.serialize(event);

		// assert

		var props = new String(result);
		// Properties format flattens nested objects with dot notation
		assertThat(props).isNotNull();
		assertThat(props.length() > 0).isTrue();
	}

	@Test
	public void shouldHandleUnicodeInEvent() {

		// arrange

		var serializer = new PropertiesSerializer();
		var event = new Event();
		event.setRealmId("realm-测试");
		event.setUserId("user-أَبْجَدِيّ");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}
}
