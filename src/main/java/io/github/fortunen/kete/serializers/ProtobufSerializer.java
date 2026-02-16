package io.github.fortunen.kete.serializers;

import static io.github.fortunen.kete.utils.ProtobufUtils.setIfPresent;
import static io.github.fortunen.kete.utils.ProtobufUtils.setMapIfPresent;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.utils.ProtobufUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "protobuf", scope = Component.SINGLETON)
public class ProtobufSerializer extends Serializer {

	private static final Descriptor EVENT_DESCRIPTOR = ProtobufUtils.findMessage("Event");
	private static final Descriptor ADMIN_EVENT_DESCRIPTOR = ProtobufUtils.findMessage("AdminEvent");
	private static final Descriptor AUTH_DETAILS_DESCRIPTOR = ProtobufUtils.findMessage("AuthDetails");

	private String contentType = "application/x-protobuf";

	@Override
	public byte[] serialize(Event event) {

		ValidationUtils.requireNonNull(event, "event is required");

		var builder = DynamicMessage.newBuilder(EVENT_DESCRIPTOR);

		setIfPresent(builder, EVENT_DESCRIPTOR, "id", event.getId());
		setIfPresent(builder, EVENT_DESCRIPTOR, "time", event.getTime());
		setIfPresent(builder, EVENT_DESCRIPTOR, "type", event.getType() != null ? event.getType().name() : null);
		setIfPresent(builder, EVENT_DESCRIPTOR, "realm_id", event.getRealmId());
		setIfPresent(builder, EVENT_DESCRIPTOR, "realm_name", event.getRealmName());
		setIfPresent(builder, EVENT_DESCRIPTOR, "client_id", event.getClientId());
		setIfPresent(builder, EVENT_DESCRIPTOR, "user_id", event.getUserId());
		setIfPresent(builder, EVENT_DESCRIPTOR, "session_id", event.getSessionId());
		setIfPresent(builder, EVENT_DESCRIPTOR, "ip_address", event.getIpAddress());
		setIfPresent(builder, EVENT_DESCRIPTOR, "error", event.getError());
		setMapIfPresent(builder, EVENT_DESCRIPTOR, "details", event.getDetails());

		return builder.build().toByteArray();
	}

	@Override
	public byte[] serialize(AdminEvent adminEvent) {

		ValidationUtils.requireNonNull(adminEvent, "adminEvent is required");

		var builder = DynamicMessage.newBuilder(ADMIN_EVENT_DESCRIPTOR);

		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "id", adminEvent.getId());
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "time", adminEvent.getTime());
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "realm_id", adminEvent.getRealmId());
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "realm_name", adminEvent.getRealmName());
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "auth_details", fromAuthDetails(adminEvent.getAuthDetails()));
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "resource_type", adminEvent.getResourceTypeAsString());
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "operation_type", adminEvent.getOperationType() != null ? adminEvent.getOperationType().name() : null);
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "resource_path", adminEvent.getResourcePath());
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "representation", adminEvent.getRepresentation());
		setIfPresent(builder, ADMIN_EVENT_DESCRIPTOR, "error", adminEvent.getError());

		return builder.build().toByteArray();
	}

	private static DynamicMessage fromAuthDetails(AuthDetails ad) {

		if (ad == null) {
			return null;
		}

		var builder = DynamicMessage.newBuilder(AUTH_DETAILS_DESCRIPTOR);

		setIfPresent(builder, AUTH_DETAILS_DESCRIPTOR, "realm_id", ad.getRealmId());
		setIfPresent(builder, AUTH_DETAILS_DESCRIPTOR, "realm_name", ad.getRealmName());
		setIfPresent(builder, AUTH_DETAILS_DESCRIPTOR, "client_id", ad.getClientId());
		setIfPresent(builder, AUTH_DETAILS_DESCRIPTOR, "user_id", ad.getUserId());
		setIfPresent(builder, AUTH_DETAILS_DESCRIPTOR, "ip_address", ad.getIpAddress());

		return builder.build();
	}
}
