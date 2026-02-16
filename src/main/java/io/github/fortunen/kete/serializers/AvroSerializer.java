package io.github.fortunen.kete.serializers;

import static io.github.fortunen.kete.utils.AvroUtils.setIfPresent;
import static io.github.fortunen.kete.utils.AvroUtils.setMapIfPresent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.EncoderFactory;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.utils.AvroUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "avro", scope = Component.SINGLETON)
public class AvroSerializer extends Serializer {

	private static final Schema EVENT_SCHEMA = AvroUtils.findSchema("Event");
	private static final Schema ADMIN_EVENT_SCHEMA = AvroUtils.findSchema("AdminEvent");
	private static final Schema AUTH_DETAILS_SCHEMA = AvroUtils.findNestedSchema(ADMIN_EVENT_SCHEMA, "auth_details");

	private String contentType = "application/avro";

	@Override
	public byte[] serialize(Event event) {

		ValidationUtils.requireNonNull(event, "event is required");

		var record = new GenericData.Record(EVENT_SCHEMA);

		setIfPresent(record, "id", event.getId());
		setIfPresent(record, "time", event.getTime());
		setIfPresent(record, "type", event.getType() != null ? event.getType().name() : null);
		setIfPresent(record, "realm_id", event.getRealmId());
		setIfPresent(record, "realm_name", event.getRealmName());
		setIfPresent(record, "client_id", event.getClientId());
		setIfPresent(record, "user_id", event.getUserId());
		setIfPresent(record, "session_id", event.getSessionId());
		setIfPresent(record, "ip_address", event.getIpAddress());
		setIfPresent(record, "error", event.getError());
		setMapIfPresent(record, "details", event.getDetails());

		return toBytes(record, EVENT_SCHEMA);
	}

	@Override
	public byte[] serialize(AdminEvent adminEvent) {

		ValidationUtils.requireNonNull(adminEvent, "adminEvent is required");

		var record = new GenericData.Record(ADMIN_EVENT_SCHEMA);

		setIfPresent(record, "id", adminEvent.getId());
		setIfPresent(record, "time", adminEvent.getTime());
		setIfPresent(record, "realm_id", adminEvent.getRealmId());
		setIfPresent(record, "realm_name", adminEvent.getRealmName());
		setIfPresent(record, "auth_details", fromAuthDetails(adminEvent.getAuthDetails()));
		setIfPresent(record, "resource_type", adminEvent.getResourceTypeAsString());
		setIfPresent(record, "operation_type", adminEvent.getOperationType() != null ? adminEvent.getOperationType().name() : null);
		setIfPresent(record, "resource_path", adminEvent.getResourcePath());
		setIfPresent(record, "representation", adminEvent.getRepresentation());
		setIfPresent(record, "error", adminEvent.getError());

		return toBytes(record, ADMIN_EVENT_SCHEMA);
	}

	private static GenericRecord fromAuthDetails(AuthDetails ad) {

		if (ad == null) {
			return null;
		}

		var record = new GenericData.Record(AUTH_DETAILS_SCHEMA);

		setIfPresent(record, "realm_id", ad.getRealmId());
		setIfPresent(record, "realm_name", ad.getRealmName());
		setIfPresent(record, "client_id", ad.getClientId());
		setIfPresent(record, "user_id", ad.getUserId());
		setIfPresent(record, "ip_address", ad.getIpAddress());

		return record;
	}

	private static byte[] toBytes(GenericRecord record, Schema schema) {
		try (var out = new ByteArrayOutputStream()) {
			var writer = new GenericDatumWriter<GenericRecord>(schema);
			var encoder = EncoderFactory.get().binaryEncoder(out, null);
			writer.write(record, encoder);
			encoder.flush();
			return out.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to serialize Avro record", e);
		}
	}
}
