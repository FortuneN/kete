package io.github.fortunen.kete.serializers;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
@Component(name = "yaml", scope = Component.SINGLETON)
public class YamlSerializer extends Serializer {

	private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
	private static final ObjectWriter EVENT_WRITER = MAPPER.writerFor(Event.class);
	private static final ObjectWriter ADMIN_EVENT_WRITER = MAPPER.writerFor(AdminEvent.class);

	private String contentType = "application/yaml";

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
