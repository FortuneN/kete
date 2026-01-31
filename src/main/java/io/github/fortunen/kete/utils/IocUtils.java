package io.github.fortunen.kete.utils;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;

@SuppressWarnings("unchecked")
public final class IocUtils {

	private IocUtils() {}

	private static final Map<String, Object> singletons = new HashMap<>();
	private static final Map<String, Class<?>> transients = new HashMap<>();

	static {

		var classes = new Reflections(Constants.PACKAGE, Scanners.TypesAnnotated)
			.getTypesAnnotatedWith(Component.class)
			.stream()
			.filter(clazz -> !clazz.isInterface())
			.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
			.collect(Collectors.toSet());

		for (var impl : classes) {

			var component = impl.getAnnotation(Component.class);

			if (ValidationUtils.isNull(component)) {
				continue;
			}

			var key = component.name();

			if (ValidationUtils.isBlank(key)) {
				continue;
			}

			if (Component.SINGLETON.equalsIgnoreCase(component.scope())) {
				singletons.put(key, newInstance(impl));
			} else {
				transients.put(key, impl);
			}
		}
	}

	public static <T> T get(String key, Class<T> clazz) {

		ValidationUtils.requireNonNull(key, "key is required");
		ValidationUtils.requireNonNull(clazz, "clazz is required");

		var singleton = singletons.get(key);

		if (ValidationUtils.isNotNull(singleton)) {
			return (T)singleton;
		}

		var transientt = transients.get(key);

		if (ValidationUtils.isNotNull(transientt)) {
			return (T)newInstance(transientt);
		}

		return null;
	}

	private static Object newInstance(Class<?> clazz) {

		ValidationUtils.requireNonNull(clazz, "clazz is required");

		try {
			return clazz.getConstructor().newInstance();
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to create " + clazz.getName() + ". Needs a no-args constructor.", exception);
		}
	}
}
