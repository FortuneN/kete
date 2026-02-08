package io.github.fortunen.kete.utils;

import java.lang.reflect.Method;
import java.io.Closeable;
import java.math.BigDecimal;
import java.net.URI;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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

	@SneakyThrows
	public static void requireTrue(BooleanSupplier condition, Supplier<? extends Throwable> exceptionSupplier) {
		if (!condition.getAsBoolean()) {
			throw exceptionSupplier.get();
		}
	}

	public static void requireTrue(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	public static void requireTrue(BooleanSupplier condition, String message) {
		if (!condition.getAsBoolean()) {
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

	@SneakyThrows
	public static void requireFalse(BooleanSupplier condition, Supplier<? extends Throwable> exceptionSupplier) {
		if (condition.getAsBoolean()) {
			throw exceptionSupplier.get();
		}
	}

	public static void requireFalse(boolean condition, String message) {
		if (condition) {
			throw new IllegalStateException(message);
		}
	}

	public static void requireFalse(BooleanSupplier condition, String message) {
		if (condition.getAsBoolean()) {
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

	@SneakyThrows
	public static <T> T requireEqual(T value, T expected, Supplier<? extends Throwable> exceptionSupplier) {

		if (value == null ? expected != null : !value.equals(expected)) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static <T> T requireEqual(T value, T expected, String message) {

		if (value == null ? expected != null : !value.equals(expected)) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	// Else methods - return default value instead of throwing

	public static <T> T requireNonNullElse(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	public static <T> T requireNonNullElseGet(T value, Supplier<T> defaultSupplier) {
		return value != null ? value : defaultSupplier.get();
	}

	public static String requireNonBlankElse(String value, String defaultValue) {
		return StringUtils.isBlank(value) ? defaultValue : value;
	}

	public static String requireNonBlankElseGet(String value, Supplier<String> defaultSupplier) {
		return StringUtils.isBlank(value) ? defaultSupplier.get() : value;
	}

	public static int requirePositiveElse(int value, int defaultValue) {
		return value <= 0 ? defaultValue : value;
	}

	public static long requirePositiveElse(long value, long defaultValue) {
		return value <= 0 ? defaultValue : value;
	}

	public static int requireNonNegativeElse(int value, int defaultValue) {
		return value < 0 ? defaultValue : value;
	}

	public static long requireNonNegativeElse(long value, long defaultValue) {
		return value < 0 ? defaultValue : value;
	}

	public static int requireInRangeElse(int value, int min, int max, int defaultValue) {
		return (value < min || value > max) ? defaultValue : value;
	}

	public static long requireInRangeElse(long value, long min, long max, long defaultValue) {
		return (value < min || value > max) ? defaultValue : value;
	}

	// Additional validations

	@SneakyThrows
	public static String requireNonEmpty(String value, Supplier<? extends Throwable> exceptionSupplier) {

		if (value == null || value.isEmpty()) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static String requireNonEmpty(String value, String message) {

		if (value == null || value.isEmpty()) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static <T extends Collection<?>> T requireNonEmpty(T collection, Supplier<? extends Throwable> exceptionSupplier) {

		if (collection == null || collection.isEmpty()) {
			throw exceptionSupplier.get();
		}

		return collection;
	}

	public static <T extends Collection<?>> T requireNonEmpty(T collection, String message) {

		if (collection == null || collection.isEmpty()) {
			throw new IllegalStateException(message);
		}

		return collection;
	}

	@SneakyThrows
	public static <T extends Map<?, ?>> T requireNonEmpty(T map, Supplier<? extends Throwable> exceptionSupplier) {

		if (map == null || map.isEmpty()) {
			throw exceptionSupplier.get();
		}

		return map;
	}

	public static <T extends Map<?, ?>> T requireNonEmpty(T map, String message) {

		if (map == null || map.isEmpty()) {
			throw new IllegalStateException(message);
		}

		return map;
	}

	@SneakyThrows
	public static <T> T[] requireNonEmpty(T[] array, Supplier<? extends Throwable> exceptionSupplier) {

		if (array == null || array.length == 0) {
			throw exceptionSupplier.get();
		}

		return array;
	}

	public static <T> T[] requireNonEmpty(T[] array, String message) {

		if (array == null || array.length == 0) {
			throw new IllegalStateException(message);
		}

		return array;
	}

	@SneakyThrows
	public static <T> T requireInstanceOf(Object value, Class<T> clazz, Supplier<? extends Throwable> exceptionSupplier) {

		if (value == null || !clazz.isInstance(value)) {
			throw exceptionSupplier.get();
		}

		return clazz.cast(value);
	}

	public static <T> T requireInstanceOf(Object value, Class<T> clazz, String message) {

		if (value == null || !clazz.isInstance(value)) {
			throw new IllegalStateException(message);
		}

		return clazz.cast(value);
	}

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
	public static String requireMatches(String value, Pattern pattern, Supplier<? extends Throwable> exceptionSupplier) {

		if (value == null || !pattern.matcher(value).matches()) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static String requireMatches(String value, Pattern pattern, String message) {

		if (value == null || !pattern.matcher(value).matches()) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static String requireMatches(String value, String regex, Supplier<? extends Throwable> exceptionSupplier) {
		if (value == null || !value.matches(regex)) {
			throw exceptionSupplier.get();
		}
		return value;
	}

	public static String requireMatches(String value, String regex, String message) {
		if (value == null || !value.matches(regex)) {
			throw new IllegalStateException(message);
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

	@SneakyThrows
	public static int requireLessThan(int value, int max, Supplier<? extends Throwable> exceptionSupplier) {

		if (value >= max) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static int requireLessThan(int value, int max, String message) {

		if (value >= max) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static long requireLessThan(long value, long max, Supplier<? extends Throwable> exceptionSupplier) {

		if (value >= max) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static long requireLessThan(long value, long max, String message) {

		if (value >= max) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static int requireGreaterThanOrEqual(int value, int min, Supplier<? extends Throwable> exceptionSupplier) {

		if (value < min) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static int requireGreaterThanOrEqual(int value, int min, String message) {

		if (value < min) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static long requireGreaterThanOrEqual(long value, long min, Supplier<? extends Throwable> exceptionSupplier) {

		if (value < min) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static long requireGreaterThanOrEqual(long value, long min, String message) {

		if (value < min) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static int requireLessThanOrEqual(int value, int max, Supplier<? extends Throwable> exceptionSupplier) {

		if (value > max) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static int requireLessThanOrEqual(int value, int max, String message) {

		if (value > max) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	@SneakyThrows
	public static long requireLessThanOrEqual(long value, long max, Supplier<? extends Throwable> exceptionSupplier) {

		if (value > max) {
			throw exceptionSupplier.get();
		}

		return value;
	}

	public static long requireLessThanOrEqual(long value, long max, String message) {

		if (value > max) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	public static int checkIndex(int index, int length) {

		if (index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
		}

		return index;
	}

	public static int checkFromToIndex(int fromIndex, int toIndex, int length) {

		if (fromIndex < 0 || fromIndex > toIndex || toIndex > length) {
			throw new IndexOutOfBoundsException("Range [" + fromIndex + ", " + toIndex + ") out of bounds for length " + length);
		}

		return fromIndex;
	}

	public static int checkFromIndexSize(int fromIndex, int size, int length) {

		if (fromIndex < 0 || size < 0 || fromIndex > length - size) {
			throw new IndexOutOfBoundsException("Range [" + fromIndex + ", " + (fromIndex + size) + ") out of bounds for length " + length);
		}

		return fromIndex;
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

	public static Optional<Long> tryParseLong(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(Long.parseLong(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<Short> tryParseShort(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(Short.parseShort(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<Byte> tryParseByte(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(Byte.parseByte(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<Double> tryParseDouble(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(Double.parseDouble(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<Float> tryParseFloat(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(Float.parseFloat(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<BigDecimal> tryParseDecimal(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(new BigDecimal(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<BigInteger> tryParseBigInteger(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(new BigInteger(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<Boolean> tryParseBoolean(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		if (value.equalsIgnoreCase("true")) {
			return Optional.of(Boolean.TRUE);
		}

		if (value.equalsIgnoreCase("false")) {
			return Optional.of(Boolean.FALSE);
		}

		return Optional.empty();
	}

	public static Optional<Boolean> tryParseBooleanLoose(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		switch (value.toLowerCase()) {
			case "true":
			case "1":
			case "yes":
			case "y":
			case "on":
				return Optional.of(Boolean.TRUE);

			case "false":
			case "0":
			case "no":
			case "n":
			case "off":
				return Optional.of(Boolean.FALSE);

			default:
				return Optional.empty();
		}
	}

	public static Optional<LocalDate> tryParseLocalDate(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<LocalDate> tryParseLocalDate(String value, DateTimeFormatter formatter) {

		if (formatter == null || isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(LocalDate.parse(value, formatter));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<LocalDateTime> tryParseLocalDateTime(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<LocalDateTime> tryParseLocalDateTime(String value, DateTimeFormatter formatter) {

		if (formatter == null || isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(LocalDateTime.parse(value, formatter));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<Instant> tryParseInstant(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(Instant.parse(value));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public static Optional<OffsetDateTime> tryParseOffsetDateTime(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(OffsetDateTime.parse(value));
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

	public static Optional<UUID> tryParseUuid(String value) {

		if (isBlank(value)) {
			return Optional.empty();
		}

		value = value.trim();

		try {
			return Optional.of(UUID.fromString(value));
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

	// is

	public static boolean isInt(String value) {
		return tryParseInt(value).isPresent();
	}

	public static boolean isLong(String value) {
		return tryParseLong(value).isPresent();
	}

	public static boolean isShort(String value) {
		return tryParseShort(value).isPresent();
	}

	public static boolean isByte(String value) {
		return tryParseByte(value).isPresent();
	}

	public static boolean isDouble(String value) {
		return tryParseDouble(value).isPresent();
	}

	public static boolean isFloat(String value) {
		return tryParseFloat(value).isPresent();
	}

	public static boolean isDecimal(String value) {
		return tryParseDecimal(value).isPresent();
	}

	public static boolean isBigInteger(String value) {
		return tryParseBigInteger(value).isPresent();
	}

	public static boolean isBoolean(String value) {
		return tryParseBoolean(value).isPresent();
	}

	public static boolean isBooleanLoose(String value) {
		return tryParseBooleanLoose(value).isPresent();
	}

	public static boolean isLocalDate(String value) {
		return tryParseLocalDate(value).isPresent();
	}

	public static boolean isLocalDate(String value, DateTimeFormatter formatter) {
		return tryParseLocalDate(value, formatter).isPresent();
	}

	public static boolean isLocalDateTime(String value) {
		return tryParseLocalDateTime(value).isPresent();
	}

	public static boolean isLocalDateTime(String value, DateTimeFormatter formatter) {
		return tryParseLocalDateTime(value, formatter).isPresent();
	}

	public static boolean isInstant(String value) {
		return tryParseInstant(value).isPresent();
	}

	public static boolean isOffsetDateTime(String value) {
		return tryParseOffsetDateTime(value).isPresent();
	}

	public static boolean isDuration(String value) {
		return tryParseDuration(value).isPresent();
	}

	public static boolean isUuid(String value) {
		return tryParseUuid(value).isPresent();
	}

	public static <TEnum extends Enum<TEnum>> boolean isEnum(Class<TEnum> enumType, String value, boolean ignoreCase) {
		return tryParseEnum(enumType, value, ignoreCase).isPresent();
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
