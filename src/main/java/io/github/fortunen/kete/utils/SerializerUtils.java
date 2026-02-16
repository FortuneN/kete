package io.github.fortunen.kete.utils;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Serializer;
import lombok.SneakyThrows;

public final class SerializerUtils {

	private SerializerUtils() {}

	@SneakyThrows
	public static Serializer createSerializer(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var kind = ValidationUtils.requireNonBlank(configuration.getString(Constants.KIND, "").trim(), Constants.KIND + " is required");
		var serializer = ValidationUtils.requireNonNull(IocUtils.get(kind, Serializer.class), "serializer " + Constants.KIND + " '" + kind + "' not found");

		serializer.setConfiguration(configuration);
		serializer.initialize();

		return serializer;
	}
}
