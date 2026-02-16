package io.github.fortunen.kete.unittests.serializers.multipartformserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import io.github.fortunen.kete.serializers.MultipartFormSerializer;

public class serializeTests {

	private static final String CRLF = "\r\n";
	private static final String BOUNDARY = "kete-boundary";

	@Test
	public void shouldSerializeEventWithAllFields() {

		// arrange

		var serializer = createSerializer("bracket");

		var event = new Event();
		event.setId("evt-001");
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");
		event.setRealmName("Test Realm");
		event.setClientId("my-client");
		event.setUserId("user-123");
		event.setSessionId("session-456");
		event.setIpAddress("192.168.1.1");
		event.setTime(1707500000000L);
		event.setDetails(Map.of("auth_method", "password"));

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("id", "evt-001"));
		assertThat(result).contains(part("type", "LOGIN"));
		assertThat(result).contains(part("realmId", "test-realm"));
		assertThat(result).contains(part("realmName", "Test Realm"));
		assertThat(result).contains(part("clientId", "my-client"));
		assertThat(result).contains(part("userId", "user-123"));
		assertThat(result).contains(part("sessionId", "session-456"));
		assertThat(result).contains(part("ipAddress", "192.168.1.1"));
		assertThat(result).contains(part("time", "1707500000000"));
		assertThat(result).contains(part("details[auth_method]", "password"));
		assertThat(result).endsWith("--" + BOUNDARY + "--" + CRLF);
	}

	@Test
	public void shouldSerializeAdminEventWithAllFields() {

		// arrange

		var serializer = createSerializer("bracket");

		var adminEvent = new AdminEvent();
		adminEvent.setId("adm-001");
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setResourceType(ResourceType.USER);
		adminEvent.setRealmId("test-realm");
		adminEvent.setRealmName("Test Realm");
		adminEvent.setResourcePath("users/abc-123");
		adminEvent.setRepresentation("{\"username\":\"john\"}");
		adminEvent.setTime(1707500000000L);

		var authDetails = new AuthDetails();
		authDetails.setRealmId("auth-realm");
		authDetails.setRealmName("Auth Realm");
		authDetails.setClientId("admin-cli");
		authDetails.setUserId("admin-uuid");
		authDetails.setIpAddress("10.0.0.1");
		adminEvent.setAuthDetails(authDetails);

		// act

		var result = new String(serializer.serialize(adminEvent), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("id", "adm-001"));
		assertThat(result).contains(part("operationType", "CREATE"));
		assertThat(result).contains(part("resourceType", "USER"));
		assertThat(result).contains(part("realmId", "test-realm"));
		assertThat(result).contains(part("authDetails[realmId]", "auth-realm"));
		assertThat(result).contains(part("authDetails[clientId]", "admin-cli"));
		assertThat(result).contains(part("authDetails[userId]", "admin-uuid"));
		assertThat(result).contains(part("authDetails[ipAddress]", "10.0.0.1"));
		assertThat(result).endsWith("--" + BOUNDARY + "--" + CRLF);
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = createSerializer("bracket");

		// act

		var thrown = catchThrowable(() -> serializer.serialize((Event) null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("event is required");
	}

	@Test
	public void shouldThrowWhenAdminEventIsNull() {

		// arrange

		var serializer = createSerializer("bracket");

		// act

		var thrown = catchThrowable(() -> serializer.serialize((AdminEvent) null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("adminEvent is required");
	}

	@Test
	public void shouldOmitNullEventFields() {

		// arrange

		var serializer = createSerializer("bracket");

		var event = new Event();
		event.setType(EventType.LOGIN);

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("type", "LOGIN"));
		assertThat(result).contains(part("time", "0"));
		assertThat(result).doesNotContain("name=\"id\"");
		assertThat(result).doesNotContain("name=\"realmId\"");
		assertThat(result).doesNotContain("name=\"clientId\"");
		assertThat(result).doesNotContain("name=\"userId\"");
		assertThat(result).doesNotContain("name=\"sessionId\"");
		assertThat(result).doesNotContain("name=\"ipAddress\"");
		assertThat(result).doesNotContain("name=\"error\"");
		assertThat(result).doesNotContain("name=\"details");
	}

	@Test
	public void shouldOmitNullAdminEventFields() {

		// arrange

		var serializer = createSerializer("bracket");

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.DELETE);

		// act

		var result = new String(serializer.serialize(adminEvent), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("operationType", "DELETE"));
		assertThat(result).doesNotContain("name=\"id\"");
		assertThat(result).doesNotContain("name=\"realmId\"");
		assertThat(result).doesNotContain("name=\"representation\"");
		assertThat(result).doesNotContain("name=\"authDetails");
	}

	@Test
	public void shouldUseBracketNotationForDetailsMap() {

		// arrange

		var serializer = createSerializer("bracket");

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setDetails(Map.of("auth_method", "password"));

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("details[auth_method]", "password"));
	}

	@Test
	public void shouldUseDotNotationForDetailsMap() {

		// arrange

		var serializer = createSerializer("dot");

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setDetails(Map.of("auth_method", "password"));

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("details.auth_method", "password"));
	}

	@Test
	public void shouldHandleUnicodeInEvent() {

		// arrange

		var serializer = createSerializer("bracket");

		var event = new Event();
		event.setUserId("用户-abc");
		event.setType(EventType.LOGIN);

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("userId", "用户-abc"));
	}

	@Test
	public void shouldFormatPartsWithCorrectMimeStructure() {

		// arrange

		var serializer = createSerializer("bracket");

		var event = new Event();
		event.setId("evt-001");
		event.setType(EventType.LOGIN);

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).startsWith("--" + BOUNDARY + CRLF);
		assertThat(result).endsWith("--" + BOUNDARY + "--" + CRLF);
		assertThat(result).contains("Content-Disposition: form-data; name=\"id\"" + CRLF + CRLF + "evt-001" + CRLF);
	}

	@Test
	public void shouldSerializeAdminEventAuthDetailsWithDotNotation() {

		// arrange

		var serializer = createSerializer("dot");

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);

		var authDetails = new AuthDetails();
		authDetails.setClientId("admin-cli");
		authDetails.setUserId("admin-uuid");
		adminEvent.setAuthDetails(authDetails);

		// act

		var result = new String(serializer.serialize(adminEvent), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains(part("authDetails.clientId", "admin-cli"));
		assertThat(result).contains(part("authDetails.userId", "admin-uuid"));
	}

	private MultipartFormSerializer createSerializer(String nestingNotation) {

		var serializer = new MultipartFormSerializer();
		var map = new HashMap<String, Object>();
		map.put("nesting-notation", nestingNotation);
		serializer.setConfiguration(new MapConfiguration(map));
		serializer.initialize();
		return serializer;
	}

	private String part(String name, String value) {

		return "--" + BOUNDARY + CRLF
			+ "Content-Disposition: form-data; name=\"" + name + "\"" + CRLF
			+ CRLF
			+ value + CRLF;
	}
}
