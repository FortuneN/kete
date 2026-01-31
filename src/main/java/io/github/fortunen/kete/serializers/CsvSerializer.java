package io.github.fortunen.kete.serializers;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Serializer;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "csv", scope = Component.SINGLETON)
public class CsvSerializer extends Serializer {

	private static final CsvMapper MAPPER = new CsvMapper();
	private static final ObjectWriter EVENT_WRITER = MAPPER.writerFor(Event.class).with(MAPPER.schemaFor(Event.class).withHeader());
	private static final ObjectWriter ADMIN_EVENT_WRITER = MAPPER.writerFor(AdminEvent.class).with(MAPPER.schemaFor(AdminEvent.class).withHeader());

	private String contentType = "text/csv";

	@Override
	@SneakyThrows
	public byte[] serialize(Event event) {

		ValidationUtils.requireNonNull(event, "event is required");

		return EVENT_WRITER.writeValueAsBytes(event);
	}

	@Override
	@SneakyThrows
	public byte[] serialize(AdminEvent adminEvent) {

		ValidationUtils.requireNonNull(adminEvent, "adminEvent is required");

		return ADMIN_EVENT_WRITER.writeValueAsBytes(adminEvent);
	}
}
