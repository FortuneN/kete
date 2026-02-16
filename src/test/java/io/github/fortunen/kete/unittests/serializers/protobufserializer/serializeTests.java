package io.github.fortunen.kete.unittests.serializers.protobufserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.events.admin.OperationType;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;

import io.github.fortunen.kete.serializers.ProtobufSerializer;
import io.github.fortunen.kete.utils.ProtobufUtils;

public class serializeTests {

	private static final Descriptor EVENT_DESCRIPTOR = ProtobufUtils.findMessage("Event");
	private static final Descriptor ADMIN_EVENT_DESCRIPTOR = ProtobufUtils.findMessage("AdminEvent");
	private static final Descriptor AUTH_DETAILS_DESCRIPTOR = ProtobufUtils.findMessage("AuthDetails");

	// =====================================================================
	// Event round-trip tests
	// =====================================================================

	@Test
	public void shouldRoundTripEventWithAllFields() throws Exception {

		// arrange

		var serializer = new ProtobufSerializer();
		var original = new Event();
		original.setId("evt-001");
		original.setTime(1700000000000L);
		original.setType(EventType.LOGIN);
		original.setRealmId("test-realm");
		original.setRealmName("Test Realm");
		original.setClientId("my-client");
		original.setUserId("user-123");
		original.setSessionId("sess-456");
		original.setIpAddress("192.168.1.100");
		original.setError("auth_failed");
		original.setDetails(Map.of("key1", "val1", "key2", "val2"));

		// act

		var bytes = serializer.serialize(original);
		var parsed = DynamicMessage.parseFrom(EVENT_DESCRIPTOR, bytes);

		// assert

		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("id")))
				.isEqualTo("evt-001");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("time")))
				.isEqualTo(1700000000000L);
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("type")))
				.isEqualTo("LOGIN");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("realm_id")))
				.isEqualTo("test-realm");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("realm_name")))
				.isEqualTo("Test Realm");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("client_id")))
				.isEqualTo("my-client");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("user_id")))
				.isEqualTo("user-123");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("session_id")))
				.isEqualTo("sess-456");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("ip_address")))
				.isEqualTo("192.168.1.100");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("error")))
				.isEqualTo("auth_failed");

		var detailsField = EVENT_DESCRIPTOR.findFieldByName("details");
		var entryDesc = detailsField.getMessageType();
		@SuppressWarnings("unchecked")
		var entries = (java.util.List<DynamicMessage>) parsed.getField(detailsField);
		var roundTrippedDetails = new HashMap<String, String>();
		for (var entry : entries) {
			var k = (String) entry.getField(entryDesc.findFieldByName("key"));
			var v = (String) entry.getField(entryDesc.findFieldByName("value"));
			roundTrippedDetails.put(k, v);
		}
		assertThat(roundTrippedDetails).containsExactlyInAnyOrderEntriesOf(
				Map.of("key1", "val1", "key2", "val2"));
	}

	// =====================================================================
	// AdminEvent round-trip tests
	// =====================================================================

	@Test
	public void shouldRoundTripAdminEventWithAllFields() throws Exception {

		// arrange

		var serializer = new ProtobufSerializer();

		var authDetails = new AuthDetails();
		authDetails.setRealmId("auth-realm");
		authDetails.setRealmName("Auth Realm");
		authDetails.setClientId("admin-cli");
		authDetails.setUserId("admin-user");
		authDetails.setIpAddress("10.0.0.1");

		var original = new AdminEvent();
		original.setId("adm-001");
		original.setTime(1700000000000L);
		original.setRealmId("test-realm");
		original.setRealmName("Test Realm");
		original.setAuthDetails(authDetails);
		original.setResourceTypeAsString("USER");
		original.setOperationType(OperationType.CREATE);
		original.setResourcePath("users/user-123");
		original.setRepresentation("{\"username\":\"newuser\"}");
		original.setError("conflict");

		// act

		var bytes = serializer.serialize(original);
		var parsed = DynamicMessage.parseFrom(ADMIN_EVENT_DESCRIPTOR, bytes);

		// assert — top-level fields

		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("id")))
				.isEqualTo("adm-001");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("time")))
				.isEqualTo(1700000000000L);
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("realm_id")))
				.isEqualTo("test-realm");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("realm_name")))
				.isEqualTo("Test Realm");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("resource_type")))
				.isEqualTo("USER");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("operation_type")))
				.isEqualTo("CREATE");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("resource_path")))
				.isEqualTo("users/user-123");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("representation")))
				.isEqualTo("{\"username\":\"newuser\"}");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("error")))
				.isEqualTo("conflict");

		// assert — nested AuthDetails round-trip

		var parsedAuth = (DynamicMessage) parsed.getField(
				ADMIN_EVENT_DESCRIPTOR.findFieldByName("auth_details"));
		assertThat(parsedAuth.getField(AUTH_DETAILS_DESCRIPTOR.findFieldByName("realm_id")))
				.isEqualTo("auth-realm");
		assertThat(parsedAuth.getField(AUTH_DETAILS_DESCRIPTOR.findFieldByName("realm_name")))
				.isEqualTo("Auth Realm");
		assertThat(parsedAuth.getField(AUTH_DETAILS_DESCRIPTOR.findFieldByName("client_id")))
				.isEqualTo("admin-cli");
		assertThat(parsedAuth.getField(AUTH_DETAILS_DESCRIPTOR.findFieldByName("user_id")))
				.isEqualTo("admin-user");
		assertThat(parsedAuth.getField(AUTH_DETAILS_DESCRIPTOR.findFieldByName("ip_address")))
				.isEqualTo("10.0.0.1");
	}

	// =====================================================================
	// Proto3 defaults (null / empty)
	// =====================================================================

	@Test
	public void shouldRoundTripEventWithAllNullFields() throws Exception {

		// arrange

		var serializer = new ProtobufSerializer();
		var original = new Event();

		// act

		var bytes = serializer.serialize(original);
		var parsed = DynamicMessage.parseFrom(EVENT_DESCRIPTOR, bytes);

		// assert

		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("id")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("time")))
				.isEqualTo(0L);
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("type")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("realm_id")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("realm_name")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("client_id")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("user_id")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("session_id")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("ip_address")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("error")))
				.isEqualTo("");

		@SuppressWarnings("unchecked")
		var entries = (java.util.List<DynamicMessage>) parsed.getField(
				EVENT_DESCRIPTOR.findFieldByName("details"));
		assertThat(entries).isEmpty();
	}

	@Test
	public void shouldRoundTripAdminEventWithNullAuthDetails() throws Exception {

		// arrange

		var serializer = new ProtobufSerializer();
		var original = new AdminEvent();
		original.setId("adm-002");
		original.setOperationType(OperationType.DELETE);

		// act

		var bytes = serializer.serialize(original);
		var parsed = DynamicMessage.parseFrom(ADMIN_EVENT_DESCRIPTOR, bytes);

		// assert

		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("id")))
				.isEqualTo("adm-002");
		assertThat(parsed.getField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("operation_type")))
				.isEqualTo("DELETE");
		assertThat(parsed.hasField(ADMIN_EVENT_DESCRIPTOR.findFieldByName("auth_details")))
				.isFalse();
	}

	// =====================================================================
	// Partial fields
	// =====================================================================

	@Test
	public void shouldRoundTripEventWithPartialFields() throws Exception {

		// arrange

		var serializer = new ProtobufSerializer();
		var original = new Event();
		original.setType(EventType.LOGOUT);
		original.setRealmId("partial-realm");

		// act

		var bytes = serializer.serialize(original);
		var parsed = DynamicMessage.parseFrom(EVENT_DESCRIPTOR, bytes);

		// assert — populated fields

		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("type")))
				.isEqualTo("LOGOUT");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("realm_id")))
				.isEqualTo("partial-realm");

		// assert — unpopulated fields get proto3 defaults

		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("id")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("user_id")))
				.isEqualTo("");
		assertThat(parsed.getField(EVENT_DESCRIPTOR.findFieldByName("session_id")))
				.isEqualTo("");
	}

	// =====================================================================
	// Null input validation
	// =====================================================================

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new ProtobufSerializer();
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

		var serializer = new ProtobufSerializer();
		AdminEvent adminEvent = null;

		// act

		var thrown = catchThrowable(() -> serializer.serialize(adminEvent));

		// assert

		assertThat(thrown)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("adminEvent is required");
	}

	// =====================================================================
	// Serialization consistency
	// =====================================================================

	@Test
	public void shouldSerializeConsistently() throws Exception {

		// arrange

		var serializer = new ProtobufSerializer();
		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("determinism-test");
		event.setUserId("user-abc");

		// act

		var bytes1 = serializer.serialize(event);
		var bytes2 = serializer.serialize(event);

		// assert

		assertThat(bytes2).isEqualTo(bytes1);
	}

	@Test
	public void shouldProduceDifferentOutputForDifferentEvents() throws Exception {

		// arrange

		var serializer = new ProtobufSerializer();
		var event1 = new Event();
		event1.setType(EventType.LOGIN);
		var event2 = new Event();
		event2.setType(EventType.LOGOUT);

		// act

		var bytes1 = serializer.serialize(event1);
		var bytes2 = serializer.serialize(event2);

		// assert

		assertThat(bytes1).isNotEqualTo(bytes2);
	}

	// =====================================================================
	// Field mapping validation (catch typos at test time)
	// =====================================================================

	@Test
	public void eventDescriptorShouldContainAllMappedFieldNames() {

		// assert

		assertThat(EVENT_DESCRIPTOR.findFieldByName("id")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("time")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("type")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("realm_id")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("realm_name")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("client_id")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("user_id")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("session_id")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("ip_address")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("error")).isNotNull();
		assertThat(EVENT_DESCRIPTOR.findFieldByName("details")).isNotNull();
	}

	@Test
	public void adminEventDescriptorShouldContainAllMappedFieldNames() {

		// assert

		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("id")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("time")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("realm_id")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("realm_name")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("auth_details")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("resource_type")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("operation_type")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("resource_path")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("representation")).isNotNull();
		assertThat(ADMIN_EVENT_DESCRIPTOR.findFieldByName("error")).isNotNull();
	}

	@Test
	public void authDetailsDescriptorShouldContainAllMappedFieldNames() {

		// assert

		assertThat(AUTH_DETAILS_DESCRIPTOR.findFieldByName("realm_id")).isNotNull();
		assertThat(AUTH_DETAILS_DESCRIPTOR.findFieldByName("realm_name")).isNotNull();
		assertThat(AUTH_DETAILS_DESCRIPTOR.findFieldByName("client_id")).isNotNull();
		assertThat(AUTH_DETAILS_DESCRIPTOR.findFieldByName("user_id")).isNotNull();
		assertThat(AUTH_DETAILS_DESCRIPTOR.findFieldByName("ip_address")).isNotNull();
	}
}
