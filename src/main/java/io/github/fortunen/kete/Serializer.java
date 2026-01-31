package io.github.fortunen.kete;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public abstract class Serializer {

	protected String contentType;

	public abstract byte[] serialize(Event event);
	public abstract byte[] serialize(AdminEvent adminEvent);
}
