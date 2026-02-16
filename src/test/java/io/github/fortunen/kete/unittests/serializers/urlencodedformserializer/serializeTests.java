package io.github.fortunen.kete.unittests.serializers.urlencodedformserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.net.URLDecoder;
import java.net.URLEncoder;
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

import io.github.fortunen.kete.serializers.UrlEncodedFormSerializer;

public class serializeTests {

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

		assertThat(result).contains("id=evt-001");
		assertThat(result).contains("type=LOGIN");
		assertThat(result).contains("realmId=test-realm");
		assertThat(result).contains("realmName=" + URLEncoder.encode("Test Realm", StandardCharsets.UTF_8));
		assertThat(result).contains("clientId=my-client");
		assertThat(result).contains("userId=user-123");
		assertThat(result).contains("sessionId=session-456");
		assertThat(result).contains("ipAddress=192.168.1.1");
		assertThat(result).contains("time=1707500000000");
		assertThat(result).contains(URLEncoder.encode("details[auth_method]", StandardCharsets.UTF_8) + "=password");
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

		assertThat(result).contains("id=adm-001");
		assertThat(result).contains("operationType=CREATE");
		assertThat(result).contains("resourceType=USER");
		assertThat(result).contains("realmId=test-realm");
		assertThat(result).contains("time=1707500000000");
		assertThat(result).contains(URLEncoder.encode("authDetails[realmId]", StandardCharsets.UTF_8) + "=auth-realm");
		assertThat(result).contains(URLEncoder.encode("authDetails[clientId]", StandardCharsets.UTF_8) + "=admin-cli");
		assertThat(result).contains(URLEncoder.encode("authDetails[userId]", StandardCharsets.UTF_8) + "=admin-uuid");
		assertThat(result).contains(URLEncoder.encode("authDetails[ipAddress]", StandardCharsets.UTF_8) + "=10.0.0.1");
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

		assertThat(result).contains("type=LOGIN");
		assertThat(result).contains("time=0");
		assertThat(result).doesNotContain("id=");
		assertThat(result).doesNotContain("realmId=");
		assertThat(result).doesNotContain("clientId=");
		assertThat(result).doesNotContain("userId=");
		assertThat(result).doesNotContain("sessionId=");
		assertThat(result).doesNotContain("ipAddress=");
		assertThat(result).doesNotContain("error=");
		assertThat(result).doesNotContain("details");
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

		assertThat(result).contains("operationType=DELETE");
		assertThat(result).contains("time=0");
		assertThat(result).doesNotContain("id=");
		assertThat(result).doesNotContain("realmId=");
		assertThat(result).doesNotContain("representation=");
		assertThat(result).doesNotContain("authDetails");
	}

	@Test
	public void shouldUrlEncodeSpecialCharacters() {

		// arrange

		var serializer = createSerializer("bracket");

		var event = new Event();
		event.setId("evt with spaces & symbols=value");
		event.setType(EventType.LOGIN);

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains("id=" + URLEncoder.encode("evt with spaces & symbols=value", StandardCharsets.UTF_8));
		assertThat(result).doesNotContain("evt with spaces");
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

		assertThat(result).contains(URLEncoder.encode("details[auth_method]", StandardCharsets.UTF_8) + "=password");
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

		assertThat(result).contains("details.auth_method=password");
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

		var decodedResult = URLDecoder.decode(result, StandardCharsets.UTF_8);
		assertThat(decodedResult).contains("userId=用户-abc");
	}

	@Test
	public void shouldHandleEventWithNoDetails() {

		// arrange

		var serializer = createSerializer("bracket");

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");

		// act

		var result = new String(serializer.serialize(event), StandardCharsets.UTF_8);

		// assert

		assertThat(result).contains("type=LOGIN");
		assertThat(result).contains("realmId=test-realm");
		assertThat(result).doesNotContain("details");
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

		assertThat(result).contains("authDetails.clientId=admin-cli");
		assertThat(result).contains("authDetails.userId=admin-uuid");
	}

	private UrlEncodedFormSerializer createSerializer(String nestingNotation) {

		var serializer = new UrlEncodedFormSerializer();
		var map = new HashMap<String, Object>();
		map.put("nesting-notation", nestingNotation);
		serializer.setConfiguration(new MapConfiguration(map));
		serializer.initialize();
		return serializer;
	}
}
