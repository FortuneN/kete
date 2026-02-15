package io.github.fortunen.kete.unittests.serializers.templateserializer;

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

import io.github.fortunen.kete.serializers.TemplateSerializer;

public class serializeTests {

	private TemplateSerializer createSerializer(String template) {
		return createSerializer(template, "none");
	}

	private TemplateSerializer createSerializer(String template, String variableEscape) {

		var serializer = new TemplateSerializer();
		var map = new HashMap<String, Object>();
		map.put("template-file-text", template);

		if (variableEscape != null) {
			map.put("variable-escape", variableEscape);
		}

		serializer.setConfiguration(new MapConfiguration(map));
		serializer.initialize();
		return serializer;
	}

	// region Event serialization

	@Test
	public void shouldSerializeEventWithAllFields() {

		// arrange

		var serializer = createSerializer("${id} ${type} ${realmId} ${realmName} ${clientId} ${userId} ${sessionId} ${ipAddress} ${time}");
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

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("evt-001 LOGIN test-realm Test Realm my-client user-123 session-456 192.168.1.1 1707500000000");
	}

	@Test
	public void shouldSerializeEventWithError() {

		// arrange

		var serializer = createSerializer("Error: ${error} for ${userId}");
		var event = new Event();
		event.setType(EventType.LOGIN_ERROR);
		event.setError("invalid_user_credentials");
		event.setUserId("user-123");

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("Error: invalid_user_credentials for user-123");
	}

	@Test
	public void shouldSerializeEventWithDetails() {

		// arrange

		var serializer = createSerializer("User ${details.username} logged in via ${details.auth_method}");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setDetails(Map.of("username", "john", "auth_method", "password"));

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("User john logged in via password");
	}

	@Test
	public void shouldSerializeEventDetailsCamelCase() {

		// arrange

		var serializer = createSerializer("Redirect: ${details.redirectUri}");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setDetails(Map.of("redirect_uri", "https://example.com/callback"));

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("Redirect: https://example.com/callback");
	}

	@Test
	public void shouldResolveUndefinedVariablesToEmptyString() {

		// arrange

		var serializer = createSerializer("Type: ${type}, Error: ${error}");
		var event = new Event();
		event.setType(EventType.LOGIN);

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("Type: LOGIN, Error: ");
	}

	@Test
	public void shouldHandleEventWithNullFields() {

		// arrange

		var serializer = createSerializer("${type} ${realmId} ${userId}");
		var event = new Event();

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("  ");
	}

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = createSerializer("test");
		Event event = null;

		// act

		var thrown = catchThrowable(() -> serializer.serialize(event));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("event is required");
	}

	// endregion

	// region AdminEvent serialization

	@Test
	public void shouldSerializeAdminEventWithAllFields() {

		// arrange

		var serializer = createSerializer("${id} ${operationType} ${resourceType} ${realmId} ${realmName} ${resourcePath} ${time}");
		var adminEvent = new AdminEvent();
		adminEvent.setId("adm-001");
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setResourceType(ResourceType.USER);
		adminEvent.setRealmId("test-realm");
		adminEvent.setRealmName("Test Realm");
		adminEvent.setResourcePath("users/abc-123");
		adminEvent.setTime(1707500000000L);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("adm-001 CREATE USER test-realm Test Realm users/abc-123 1707500000000");
	}

	@Test
	public void shouldSerializeAdminEventWithAuthDetails() {

		// arrange

		var serializer = createSerializer("${authDetails.realmId} ${authDetails.realmName} ${authDetails.userId} from ${authDetails.ipAddress} on ${authDetails.clientId}");
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		var authDetails = new AuthDetails();
		authDetails.setRealmId("auth-realm");
		authDetails.setRealmName("Auth Realm");
		authDetails.setUserId("admin-uuid");
		authDetails.setIpAddress("10.0.0.1");
		authDetails.setClientId("admin-cli");
		adminEvent.setAuthDetails(authDetails);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("auth-realm Auth Realm admin-uuid from 10.0.0.1 on admin-cli");
	}

	@Test
	public void shouldSerializeAdminEventWithRepresentation() {

		// arrange

		var serializer = createSerializer("Representation: ${representation}");
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRepresentation("{\"username\":\"john\"}");

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("Representation: {\"username\":\"john\"}");
	}

	@Test
	public void shouldHandleAdminEventWithNullAuthDetails() {

		// arrange

		var serializer = createSerializer("${authDetails.userId}: ${operationType}");
		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.DELETE);

		// act

		var result = serializer.serialize(adminEvent);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo(": DELETE");
	}

	@Test
	public void shouldThrowWhenAdminEventIsNull() {

		// arrange

		var serializer = createSerializer("test");
		AdminEvent adminEvent = null;

		// act

		var thrown = catchThrowable(() -> serializer.serialize(adminEvent));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("adminEvent is required");
	}

	// endregion

	// region Global escape

	@Test
	public void shouldApplyGlobalJsonEscape() {

		// arrange

		var serializer = createSerializer("${realmId}", "json");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm\"with\"quotes");

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("realm\\\"with\\\"quotes");
	}

	@Test
	public void shouldApplyGlobalXmlEscape() {

		// arrange

		var serializer = createSerializer("${realmId}", "xml");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm<with>&");

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("realm&lt;with&gt;&amp;");
	}

	@Test
	public void shouldApplyGlobalUrlEscape() {

		// arrange

		var serializer = createSerializer("${realmId}", "url");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm with spaces");

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("realm+with+spaces");
	}

	// endregion

	// region Per-variable filter override

	@Test
	public void shouldApplyPerVariableFilterOverride() {

		// arrange

		var serializer = createSerializer("${realmId:json} ${userId:none}", "xml");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm\"test");
		event.setUserId("user<raw>");

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("realm\\\"test user<raw>");
	}

	@Test
	public void shouldOverrideGlobalEscapeWithNone() {

		// arrange

		var serializer = createSerializer("${realmId:none}", "json");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm\"quotes");

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("realm\"quotes");
	}

	// endregion

	// region Static template (no variables)

	@Test
	public void shouldReturnStaticTemplateWithNoVariables() {

		// arrange

		var serializer = createSerializer("Hello World!");
		var event = new Event();
		event.setType(EventType.LOGIN);

		// act

		var result = serializer.serialize(event);

		// assert

		var output = new String(result, StandardCharsets.UTF_8);
		assertThat(output).isEqualTo("Hello World!");
	}

	// endregion

	// region Consistency

	@Test
	public void shouldSerializeConsistently() {

		// arrange

		var serializer = createSerializer("${type} ${realmId}");
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("test-realm");

		// act

		var result1 = serializer.serialize(event);
		var result2 = serializer.serialize(event);

		// assert

		assertThat(result2).isEqualTo(result1);
	}

	// endregion
}
