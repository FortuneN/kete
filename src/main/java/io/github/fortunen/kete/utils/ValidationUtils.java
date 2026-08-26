package io.github.fortunen.kete.utils;

import java.lang.reflect.Method;
import java.io.Closeable;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ValidationUtils {

	private ValidationUtils() {}

	// Boolean validation with custom exception

	@SneakyThrows
	public static void requireTrue(boolean condition, Supplier<? extends Throwable> exceptionSupplier) {
		if (!condition) {
			throw exceptionSupplier.get();
		}
	}

	public static void requireTrue(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	// Object validation with custom exception

	@SneakyThrows
	public static <T> T requireNonNull(T value, Supplier<? extends Throwable> exceptionSupplier) {

		if (value == null) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static <T> T requireNonNull(T value, String message) {

		if (value == null) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static void requireFalse(boolean condition, Supplier<? extends Throwable> exceptionSupplier) {
		if (condition) {
			throw exceptionSupplier.get();
		}
	}

	public static void requireFalse(boolean condition, String message) {
		if (condition) {
			throw new IllegalStateException(message);
		}
	}

	// String validation with custom exception

	@SneakyThrows
	public static String requireNonBlank(String value, Supplier<? extends Throwable> exceptionSupplier) {

		if (StringUtils.isBlank(value)) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static String requireNonBlank(String value, String message) {

		if (StringUtils.isBlank(value)) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static int requirePositive(int value, Supplier<? extends Throwable> exceptionSupplier) {

		if (value <= 0) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static int requirePositive(int value, String message) {

		if (value <= 0) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static long requirePositive(long value, Supplier<? extends Throwable> exceptionSupplier) {

		if (value <= 0) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static long requirePositive(long value, String message) {

		if (value <= 0) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static int requireNonNegative(int value, Supplier<? extends Throwable> exceptionSupplier) {

		if (value < 0) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static int requireNonNegative(int value, String message) {

		if (value < 0) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static long requireNonNegative(long value, Supplier<? extends Throwable> exceptionSupplier) {

		if (value < 0) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static long requireNonNegative(long value, String message) {

		if (value < 0) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static int requireInRange(int value, int min, int max, Supplier<? extends Throwable> exceptionSupplier) {

		if (value < min || value > max) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static int requireInRange(int value, int min, int max, String message) {

		if (value < min || value > max) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static long requireInRange(long value, long min, long max, Supplier<? extends Throwable> exceptionSupplier) {

		if (value < min || value > max) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static long requireInRange(long value, long min, long max, String message) {

		if (value < min || value > max) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	// Else methods - return default value instead of throwing

	public static <T> T requireNonNullElse(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	public static String requireNonBlankElse(String value, String defaultValue) {
		return StringUtils.isBlank(value) ? defaultValue : value;
	}

	// Additional validations

	public static String requireValidUrl(String value, String message) {

		requireNonBlank(value, message);

		try {
			var uri = URI.create(value);
			requireTrue(uri.isAbsolute(), message);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(message, e);
		}

		return value;
	}

	@SneakyThrows
	public static int requireGreaterThan(int value, int min, Supplier<? extends Throwable> exceptionSupplier) {

		if (value <= min) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static int requireGreaterThan(int value, int min, String message) {

		if (value <= min) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static long requireGreaterThan(long value, long min, Supplier<? extends Throwable> exceptionSupplier) {

		if (value <= min) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static long requireGreaterThan(long value, long min, String message) {

		if (value <= min) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	// Detection methods (non-throwing)

	public static boolean isNull(Object value) {
		return value == null;
	}

	public static boolean isNotNull(Object value) {
		return value != null;
	}

	public static boolean isBlank(String value) {
		return StringUtils.isBlank(value);
	}

	public static boolean isNotBlank(String value) {
		return StringUtils.isNotBlank(value);
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public static boolean isNotNullOrEmpty(String value) {
		return value != null && !value.isEmpty();
	}

	public static boolean isNullOrEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isNotNullOrEmpty(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}

	public static boolean isNullOrEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isNotNullOrEmpty(Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}

	public static <T> boolean isNullOrEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	public static <T> boolean isNotNullOrEmpty(T[] array) {
		return array != null && array.length > 0;
	}

	public static boolean isNullOrEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNotNullOrEmpty(byte[] array) {
		return array != null && array.length > 0;
	}

	// tryParse

	public static Optional<Integer> tryParseInt(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(Integer.parseInt(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<Duration> tryParseDuration(String value) {

		if (value == null) {
			return Optional.empty();
		}

		value = value.trim();

		if (value.isEmpty()) {
			return Optional.empty();
		}

		var lower = value.toLowerCase();

		try {

			// iso-8601, e.g. "PT15M", "PT2H", "P2DT3H4M"

			if (lower.startsWith("pt") || lower.startsWith("p")) {
				return Optional.of(Duration.parse(value));
			}

// seconds (digits only), e.g. "1500"

		var allDigits = true;

		for (var i = 0; i < lower.length(); i++) {
			if (!Character.isDigit(lower.charAt(i))) {
				allDigits = false;
				break;
			}
		}

		if (allDigits) {
			return Optional.of(Duration.ofSeconds(Long.parseLong(lower)));
			}

			// nanoseconds, e.g. "10ns"

			if (lower.endsWith("ns")) {
				var number = lower.substring(0, lower.length() - 2).trim();
				return Optional.of(Duration.ofNanos(Long.parseLong(number)));
			}

			// microseconds, e.g. "10us"

			if (lower.endsWith("us")) {
				var number = lower.substring(0, lower.length() - 2).trim();
				return Optional.of(Duration.ofNanos(Math.multiplyExact(Long.parseLong(number), 1_000)));
			}

			// milliseconds, e.g. "10ms"

			if (lower.endsWith("ms")) {
				var number = lower.substring(0, lower.length() - 2).trim();
				return Optional.of(Duration.ofMillis(Long.parseLong(number)));
			}

			// seconds, e.g. "10s"

			if (lower.endsWith("s")) {
				var number = lower.substring(0, lower.length() - 1).trim();
				return Optional.of(Duration.ofSeconds(Long.parseLong(number)));
			}

			// minutes, e.g. "10m"

			if (lower.endsWith("m")) {
				var number = lower.substring(0, lower.length() - 1).trim();
				return Optional.of(Duration.ofMinutes(Long.parseLong(number)));
			}

			// hours, e.g. "10h"

			if (lower.endsWith("h")) {
				var number = lower.substring(0, lower.length() - 1).trim();
				return Optional.of(Duration.ofHours(Long.parseLong(number)));
			}

			// days, e.g. "10d"

			if (lower.endsWith("d")) {
				var number = lower.substring(0, lower.length() - 1).trim();
				return Optional.of(Duration.ofDays(Long.parseLong(number)));
			}

			// weeks (7 days), e.g. "2w"

			if (lower.endsWith("w")) {
				var number = lower.substring(0, lower.length() - 1).trim();
				return Optional.of(Duration.ofDays(Math.multiplyExact(Long.parseLong(number), 7)));
			}

			// years (365 days), e.g. "1y"

			if (lower.endsWith("y")) {
				var number = lower.substring(0, lower.length() - 1).trim();
				return Optional.of(Duration.ofDays(Math.multiplyExact(Long.parseLong(number), 365)));
			}

			return Optional.empty();
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static <TEnum extends Enum<TEnum>> Optional<TEnum> tryParseEnum(Class<TEnum> enumType, String value, boolean ignoreCase) {

		if (enumType == null || isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {

			if (!ignoreCase) {
				return Optional.of(Enum.valueOf(enumType, value));
			}

			for (var constant : enumType.getEnumConstants()) {
				if (constant.name().equalsIgnoreCase(value)) {
					return Optional.of(constant);
				}
			}

			return Optional.empty();
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	// Resource closing

	private static final ConcurrentHashMap<Class<?>, Method> CLOSE_METHOD_CACHE = new ConcurrentHashMap<>();

	public static void tryClose(AutoCloseable resource, String resourceName) {
		if (isNotNull(resource)) {
			try {
				resource.close();
			} catch (Exception exception) {
				log.warn("Failed to close " + resourceName, exception);
			}
		}
	}

	public static void tryClose(Closeable resource, String resourceName) {
		if (isNotNull(resource)) {
			try {
				resource.close();
			} catch (Exception exception) {
				log.warn("Failed to close " + resourceName, exception);
			}
		}
	}

	public static int requireValidPort(int port, String fieldName) {
		requireTrue(port > 0 && port <= 65535, fieldName + " must be between 1 and 65535");
		return port;
	}

	public static void tryClose(Object resource, String resourceName) {

		if (isNull(resource)) {
			return;
		}

		var closeMethod = CLOSE_METHOD_CACHE.computeIfAbsent(resource.getClass(), clazz -> {

			Method method = null;

			try {
				method = clazz.getMethod("close");
			} catch (Exception e) {
				return method;
			}

			try {
				method.setAccessible(true);
			} catch (Exception e) {
				return method;
			}

			return method;
		});

		if (isNotNull(closeMethod)) {
			try {
				closeMethod.invoke(resource);
			} catch (Exception exception) {
				log.warn("Failed to close " + resourceName, exception);
			}
		}
	}
}
