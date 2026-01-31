package io.github.fortunen.kete.unittests.serializers.xmlserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.serializers.XmlSerializer;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

public class serializeTests {

	@Test
	public void shouldSerializeEventToXml() {

		// arrange

		var serializer = new XmlSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setUserId("test-user");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var xml = new String(result);
		// Verify XML structure with element tags and values
		assertThat(xml).matches(Pattern.compile(".*<type>LOGIN</type>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<realmId>test-realm</realmId>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<userId>test-user</userId>.*", Pattern.DOTALL));
	}

	@Test
	public void shouldSerializeAdminEventToXml() {

		// arrange

		var serializer = new XmlSerializer();
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRealmId("test-realm");
		adminEvent.setResourceType(org.keycloak.events.admin.ResourceType.USER);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var xml = new String(result);
		// Verify XML structure with element tags and values
		assertThat(xml).matches(Pattern.compile(".*<operationType>CREATE</operationType>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<realmId>test-realm</realmId>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<resourceType>USER</resourceType>.*", Pattern.DOTALL));
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new XmlSerializer();
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

		var serializer = new XmlSerializer();
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

		var serializer = new XmlSerializer();
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

		var serializer = new XmlSerializer();
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldSerializeEventWithAllFields() {

		// arrange

		var serializer = new XmlSerializer();
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

		var xml = new String(result);
		// Verify XML structure with element tags and values
		assertThat(xml).matches(Pattern.compile(".*<type>LOGIN</type>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<realmId>realm</realmId>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<userId>user</userId>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<sessionId>session</sessionId>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<ipAddress>127.0.0.1</ipAddress>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<clientId>client</clientId>.*", Pattern.DOTALL));
		assertThat(xml).matches(Pattern.compile(".*<time>1234567890</time>.*", Pattern.DOTALL));
	}

	@Test
	public void shouldReturnApplicationXmlContentType() {

		// arrange

		var serializer = new XmlSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/xml");
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() {

		// arrange

		var serializer = new XmlSerializer();
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

		var xml1 = new String(result1);
		var xml2 = new String(result2);
		// Verify XML structure with element tags and values
		assertThat(xml1).matches(Pattern.compile(".*<type>LOGIN</type>.*", Pattern.DOTALL));
		assertThat(xml2).matches(Pattern.compile(".*<type>LOGOUT</type>.*", Pattern.DOTALL));
	}

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = new XmlSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test");

		// act

		var result1 = serializer.serialize(event);
		var result2 = serializer.serialize(event);

		// assert

		assertThat(result2).isEqualTo(result1);
	}
}
