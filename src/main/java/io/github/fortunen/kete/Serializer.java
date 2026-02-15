package io.github.fortunen.kete;

import org.apache.commons.configuration2.MapConfiguration;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public abstract class Serializer {

	protected String contentType;
	protected MapConfiguration configuration;

	public void initialize() {}

	public abstract byte[] serialize(Event event);
	public abstract byte[] serialize(AdminEvent adminEvent);
}
