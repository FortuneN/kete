package io.github.fortunen.kete.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.DestinationConfig;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
public final class DestinationUtils {

	private DestinationUtils() {}

	private static final ConcurrentHashMap<String, Constructor<? extends DestinationConfig>> CONFIG_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

	public static String generateClientIdPrefix() {
		return Constants.ID + "-" + UUID.randomUUID().toString().toLowerCase(Locale.ROOT);
	}

	@SneakyThrows
	public static DestinationConfig createDestinationConfig(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var destinationKind = ValidationUtils.requireNonBlank(configuration.getString(Constants.KIND, "").trim(), Constants.KIND + " is required");

		var constructor = CONFIG_CONSTRUCTOR_CACHE.computeIfAbsent(destinationKind, kind -> {

			Destination<?> destination = null;

			try {

				destination = ValidationUtils.requireNonNull(IocUtils.get(kind, Destination.class), "destination " + Constants.KIND + " '" + kind + "' not found");

				var genericSuperclass = destination.getClass().getGenericSuperclass();

				ValidationUtils.requireTrue(genericSuperclass instanceof ParameterizedType, "Destination must have generic type parameter");

				var parameterizedType = (ParameterizedType) genericSuperclass;
				var typeArguments = parameterizedType.getActualTypeArguments();

				ValidationUtils.requireTrue(typeArguments.length == 1, "Destination must have exactly one type parameter");

				var configClass = (Class<? extends DestinationConfig>) typeArguments[0];
				var configConstructor = configClass.getConstructor();

				configConstructor.setAccessible(true);

				return configConstructor;

			} catch (Exception exception) {
				throw new RuntimeException("Failed to resolve DestinationConfig for kind '" + kind + "'", exception);
			} finally {
				ValidationUtils.tryClose(destination, "destination");
			}
		});

		var config = constructor.newInstance();

		config.setConfiguration(configuration);
		config.setDestinationKind(destinationKind);

		return config;
	}

	public static Destination<?> createDestination(DestinationConfig configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		var destination = ValidationUtils.requireNonNull(IocUtils.get(configuration.getDestinationKind(), Destination.class), "destination " + Constants.KIND + " '" + configuration.getDestinationKind() + "' not found");

		destination.setConfig(configuration);

		return destination;
	}
}
