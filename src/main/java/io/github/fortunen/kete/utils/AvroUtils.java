package io.github.fortunen.kete.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

public final class AvroUtils {

	private AvroUtils() {}

	private static final Map<String, Schema> SCHEMAS = new HashMap<>();

	static {
		SCHEMAS.put("Event", loadSchema("schemas/avro/event.avsc"));
		SCHEMAS.put("AdminEvent", loadSchema("schemas/avro/admin_event.avsc"));
	}

	private static Schema loadSchema(String resourcePath) {
		try (var stream = AvroUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (stream == null) {
				throw new IOException("Schema resource not found on classpath: " + resourcePath);
			}
			return new Schema.Parser().parse(stream);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load Avro schema from " + resourcePath, e);
		}
	}

	public static Schema findSchema(String name) {
		var schema = SCHEMAS.get(name);
		if (schema == null) {
			throw new IllegalStateException("Schema not found: " + name);
		}
		return schema;
	}

	public static Schema findNestedSchema(Schema parentSchema, String fieldName) {
		var field = parentSchema.getField(fieldName);
		if (field == null) {
			throw new IllegalStateException("Field not found in schema: " + fieldName);
		}
		var unionSchema = field.schema();
		for (var type : unionSchema.getTypes()) {
			if (type.getType() == Schema.Type.RECORD) {
				return type;
			}
		}
		throw new IllegalStateException("No record type found in union for field: " + fieldName);
	}

	public static void setIfPresent(GenericRecord record, String fieldName, Object value) {
		if (value != null && record.getSchema().getField(fieldName) != null) {
			record.put(fieldName, value);
		}
	}

	public static void setMapIfPresent(GenericRecord record, String fieldName, Map<String, String> map) {
		if (map != null && record.getSchema().getField(fieldName) != null) {
			record.put(fieldName, new HashMap<>(map));
		}
	}
}
