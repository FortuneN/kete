package io.github.fortunen.kete;

import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.admin.AdminEvent;

@Data
public class Provider implements EventListenerProvider {

	private EventListenerTransaction transaction;

	public Provider(EventListenerTransaction transaction) {
		this.transaction = ValidationUtils.requireNonNull(transaction, "transaction is required");
	}

	@Override
	public void onEvent(Event event) {
		transaction.addEvent(event);
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		transaction.addAdminEvent(event, includeRepresentation);
	}

	@Override
	public void close() {}
}
