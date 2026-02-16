package io.github.fortunen.kete.unittests.serializers.avroserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.events.admin.OperationType;

import io.github.fortunen.kete.serializers.AvroSerializer;
import io.github.fortunen.kete.utils.AvroUtils;

public class serializeTests {

	private static final Schema EVENT_SCHEMA = AvroUtils.findSchema("Event");
	private static final Schema ADMIN_EVENT_SCHEMA = AvroUtils.findSchema("AdminEvent");
	private static final Schema AUTH_DETAILS_SCHEMA = AvroUtils.findNestedSchema(ADMIN_EVENT_SCHEMA, "auth_details");

	// =====================================================================
	// Event round-trip tests
	// =====================================================================

	@Test
	public void shouldRoundTripEventWithAllFields() throws Exception {

		// arrange

		var serializer = new AvroSerializer();
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
		var parsed = deserialize(bytes, EVENT_SCHEMA);

		// assert

		assertThat(parsed.get("id")).hasToString("evt-001");
		assertThat(parsed.get("time")).isEqualTo(1700000000000L);
		assertThat(parsed.get("type")).hasToString("LOGIN");
		assertThat(parsed.get("realm_id")).hasToString("test-realm");
		assertThat(parsed.get("realm_name")).hasToString("Test Realm");
		assertThat(parsed.get("client_id")).hasToString("my-client");
		assertThat(parsed.get("user_id")).hasToString("user-123");
		assertThat(parsed.get("session_id")).hasToString("sess-456");
		assertThat(parsed.get("ip_address")).hasToString("192.168.1.100");
		assertThat(parsed.get("error")).hasToString("auth_failed");

		@SuppressWarnings("unchecked")
		var details = (Map<CharSequence, CharSequence>) parsed.get("details");
		var roundTrippedDetails = new HashMap<String, String>();
		details.forEach((k, v) -> roundTrippedDetails.put(k.toString(), v.toString()));
		assertThat(roundTrippedDetails).containsExactlyInAnyOrderEntriesOf(
				Map.of("key1", "val1", "key2", "val2"));
	}

	// =====================================================================
	// AdminEvent round-trip tests
	// =====================================================================

	@Test
	public void shouldRoundTripAdminEventWithAllFields() throws Exception {

		// arrange

		var serializer = new AvroSerializer();

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
		var parsed = deserialize(bytes, ADMIN_EVENT_SCHEMA);

		// assert — top-level fields

		assertThat(parsed.get("id")).hasToString("adm-001");
		assertThat(parsed.get("time")).isEqualTo(1700000000000L);
		assertThat(parsed.get("realm_id")).hasToString("test-realm");
		assertThat(parsed.get("realm_name")).hasToString("Test Realm");
		assertThat(parsed.get("resource_type")).hasToString("USER");
		assertThat(parsed.get("operation_type")).hasToString("CREATE");
		assertThat(parsed.get("resource_path")).hasToString("users/user-123");
		assertThat(parsed.get("representation")).hasToString("{\"username\":\"newuser\"}");
		assertThat(parsed.get("error")).hasToString("conflict");

		// assert — nested AuthDetails round-trip

		var parsedAuth = (GenericRecord) parsed.get("auth_details");
		assertThat(parsedAuth.get("realm_id")).hasToString("auth-realm");
		assertThat(parsedAuth.get("realm_name")).hasToString("Auth Realm");
		assertThat(parsedAuth.get("client_id")).hasToString("admin-cli");
		assertThat(parsedAuth.get("user_id")).hasToString("admin-user");
		assertThat(parsedAuth.get("ip_address")).hasToString("10.0.0.1");
	}

	// =====================================================================
	// Avro null handling (union defaults)
	// =====================================================================

	@Test
	public void shouldRoundTripEventWithAllNullFields() throws Exception {

		// arrange

		var serializer = new AvroSerializer();
		var original = new Event();

		// act

		var bytes = serializer.serialize(original);
		var parsed = deserialize(bytes, EVENT_SCHEMA);

		// assert — Avro nullable unions return null, not defaults
		// Note: Event.getTime() returns primitive long (default 0L), so it serializes as 0L not null

		assertThat(parsed.get("id")).isNull();
		assertThat(parsed.get("time")).isEqualTo(0L);
		assertThat(parsed.get("type")).isNull();
		assertThat(parsed.get("realm_id")).isNull();
		assertThat(parsed.get("realm_name")).isNull();
		assertThat(parsed.get("client_id")).isNull();
		assertThat(parsed.get("user_id")).isNull();
		assertThat(parsed.get("session_id")).isNull();
		assertThat(parsed.get("ip_address")).isNull();
		assertThat(parsed.get("error")).isNull();
		assertThat(parsed.get("details")).isNull();
	}

	@Test
	public void shouldRoundTripAdminEventWithNullAuthDetails() throws Exception {

		// arrange

		var serializer = new AvroSerializer();
		var original = new AdminEvent();
		original.setId("adm-002");
		original.setOperationType(OperationType.DELETE);

		// act

		var bytes = serializer.serialize(original);
		var parsed = deserialize(bytes, ADMIN_EVENT_SCHEMA);

		// assert

		assertThat(parsed.get("id")).hasToString("adm-002");
		assertThat(parsed.get("operation_type")).hasToString("DELETE");
		assertThat(parsed.get("auth_details")).isNull();
	}

	// =====================================================================
	// Partial fields
	// =====================================================================

	@Test
	public void shouldRoundTripEventWithPartialFields() throws Exception {

		// arrange

		var serializer = new AvroSerializer();
		var original = new Event();
		original.setType(EventType.LOGOUT);
		original.setRealmId("partial-realm");

		// act

		var bytes = serializer.serialize(original);
		var parsed = deserialize(bytes, EVENT_SCHEMA);

		// assert — populated fields

		assertThat(parsed.get("type")).hasToString("LOGOUT");
		assertThat(parsed.get("realm_id")).hasToString("partial-realm");

		// assert — unpopulated fields are null (Avro union default)

		assertThat(parsed.get("id")).isNull();
		assertThat(parsed.get("user_id")).isNull();
		assertThat(parsed.get("session_id")).isNull();
	}

	// =====================================================================
	// Null input validation
	// =====================================================================

	@Test
	public void shouldThrowWhenEventIsNull() {

		// arrange

		var serializer = new AvroSerializer();
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

		var serializer = new AvroSerializer();
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

		var serializer = new AvroSerializer();
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

		var serializer = new AvroSerializer();
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
	public void eventSchemaShouldContainAllMappedFieldNames() {

		// assert

		assertThat(EVENT_SCHEMA.getField("id")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("time")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("type")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("realm_id")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("realm_name")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("client_id")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("user_id")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("session_id")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("ip_address")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("error")).isNotNull();
		assertThat(EVENT_SCHEMA.getField("details")).isNotNull();
	}

	@Test
	public void adminEventSchemaShouldContainAllMappedFieldNames() {

		// assert

		assertThat(ADMIN_EVENT_SCHEMA.getField("id")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("time")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("realm_id")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("realm_name")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("auth_details")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("resource_type")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("operation_type")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("resource_path")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("representation")).isNotNull();
		assertThat(ADMIN_EVENT_SCHEMA.getField("error")).isNotNull();
	}

	@Test
	public void authDetailsSchemaShouldContainAllMappedFieldNames() {

		// assert

		assertThat(AUTH_DETAILS_SCHEMA.getField("realm_id")).isNotNull();
		assertThat(AUTH_DETAILS_SCHEMA.getField("realm_name")).isNotNull();
		assertThat(AUTH_DETAILS_SCHEMA.getField("client_id")).isNotNull();
		assertThat(AUTH_DETAILS_SCHEMA.getField("user_id")).isNotNull();
		assertThat(AUTH_DETAILS_SCHEMA.getField("ip_address")).isNotNull();
	}

	// =====================================================================
	// Helpers
	// =====================================================================

	private static GenericRecord deserialize(byte[] bytes, Schema schema) throws Exception {
		var reader = new GenericDatumReader<GenericRecord>(schema);
		var decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(bytes), null);
		return reader.read(null, decoder);
	}
}
