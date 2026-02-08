package io.github.fortunen.kete.unittests.serializers.tomlserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.serializers.TomlSerializer;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

public class serializeTests {

	@Test
	public void shouldSerializeEventToToml() {

		// arrange

		var serializer = new TomlSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setUserId("test-user");

		// act

		var result = serializer.serialize(event);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var toml = new String(result);
		// Verify TOML key=value format
		assertThat(toml).matches(Pattern.compile(".*type\\s*=\\s*[\"']?LOGIN[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*realmId\\s*=\\s*[\"']?test-realm[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*userId\\s*=\\s*[\"']?test-user[\"']?.*", Pattern.DOTALL));
	}

	@Test
	public void shouldSerializeAdminEventToToml() {

		// arrange

		var serializer = new TomlSerializer();
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRealmId("test-realm");
		adminEvent.setResourceType(ResourceType.USER);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();

		var toml = new String(result);
		// Verify TOML key=value format
		assertThat(toml).matches(Pattern.compile(".*operationType\\s*=\\s*[\"']?CREATE[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*realmId\\s*=\\s*[\"']?test-realm[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*resourceType\\s*=\\s*[\"']?USER[\"']?.*", Pattern.DOTALL));
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new TomlSerializer();
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

		var serializer = new TomlSerializer();
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

		var serializer = new TomlSerializer();
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

		var serializer = new TomlSerializer();
		var adminEvent = new AdminEvent();

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length > 0).isTrue();
	}

	@Test
	public void shouldReturnApplicationTomlContentType() {

		// arrange

		var serializer = new TomlSerializer();

		// act

		var result = serializer.getContentType();

		// assert

		assertThat(result).isEqualTo("application/toml");
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() {

		// arrange

		var serializer = new TomlSerializer();
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

		var toml1 = new String(result1);
		var toml2 = new String(result2);
		// Verify TOML key=value format
		assertThat(toml1).matches(Pattern.compile(".*type\\s*=\\s*[\"']?LOGIN[\"']?.*", Pattern.DOTALL));
		assertThat(toml2).matches(Pattern.compile(".*type\\s*=\\s*[\"']?LOGOUT[\"']?.*", Pattern.DOTALL));
	}

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = new TomlSerializer();
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
	public void shouldSerializeEventWithAllFields() {

		// arrange

		var serializer = new TomlSerializer();
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

		var toml = new String(result);
		// Verify TOML key=value format
		assertThat(toml).matches(Pattern.compile(".*type\\s*=\\s*[\"']?LOGIN[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*realmId\\s*=\\s*[\"']?realm[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*userId\\s*=\\s*[\"']?user[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*sessionId\\s*=\\s*[\"']?session[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*ipAddress\\s*=\\s*[\"']?127\\.0\\.0\\.1[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*clientId\\s*=\\s*[\"']?client[\"']?.*", Pattern.DOTALL));
		assertThat(toml).matches(Pattern.compile(".*time\\s*=\\s*1234567890.*", Pattern.DOTALL));
	}

	@Test
	public void shouldUseKeyValueFormat() {

		// arrange

		var serializer = new TomlSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test");

		// act

		var result = serializer.serialize(event);

		// assert

		var toml = new String(result);
		// TOML uses key=value format
		assertThat(toml.contains("=") || toml.contains("\n")).isTrue();
	}

	@Test
	public void shouldHandleUnicodeInEvent() {

		// arrange

		var serializer = new TomlSerializer();
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
