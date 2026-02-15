package io.github.fortunen.kete;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.CaseFormat;

import io.github.fortunen.kete.utils.ValidationUtils;

public record EventMessage(String realm, String eventId, byte[] eventBody, String eventType, String contentType, String resourceType, String kind, String operationType, String result) {

	private static final int MAX_CACHE_SIZE = 1000;

	private static final Map<String, byte[]> BYTES = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
			return size() > MAX_CACHE_SIZE;
		}
	});

	private static final Map<String, String> LOWERCASE_CACHE = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
			return size() > MAX_CACHE_SIZE;
		}
	});

	private static final Map<String, String> UPPERCASE_CACHE = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
			return size() > MAX_CACHE_SIZE;
		}
	});

	private static final Map<String, String> KEBAB_CACHE = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
			return size() > MAX_CACHE_SIZE;
		}
	});

	private static final Map<String, String> PASCAL_CACHE = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
			return size() > MAX_CACHE_SIZE;
		}
	});

	private static final Map<String, String> CAMEL_CACHE = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
			return size() > MAX_CACHE_SIZE;
		}
	});

	public String kindLowerCase() {

		if (ValidationUtils.isBlank(kind)) {
			return null;
		}

		return LOWERCASE_CACHE.computeIfAbsent(kind, String::toLowerCase);
	}

	public String kindUpperCase() {

		if (ValidationUtils.isBlank(kind)) {
			return null;
		}

		return UPPERCASE_CACHE.computeIfAbsent(kind, String::toUpperCase);
	}

	public byte[] kindBytes() {

		if (ValidationUtils.isBlank(kind)) {
			return null;
		}

		return BYTES.computeIfAbsent(kind, String::getBytes);
	}

	public String eventIdLowerCase() {

		if (ValidationUtils.isBlank(eventId)) {
			return null;
		}

		return eventId.toLowerCase();
	}

	public String eventIdUpperCase() {

		if (ValidationUtils.isBlank(eventId)) {
			return null;
		}

		return eventId.toUpperCase();
	}

	public String realmLowerCase() {

		if (ValidationUtils.isBlank(realm)) {
			return null;
		}

		return LOWERCASE_CACHE.computeIfAbsent(realm, String::toLowerCase);
	}

	public String realmUpperCase() {

		if (ValidationUtils.isBlank(realm)) {
			return null;
		}

		return UPPERCASE_CACHE.computeIfAbsent(realm, String::toUpperCase);
	}

	public String eventTypeLowerCase() {

		if (ValidationUtils.isBlank(eventType)) {
			return null;
		}

		return LOWERCASE_CACHE.computeIfAbsent(eventType, String::toLowerCase);
	}

	public String eventTypeUpperCase() {

		if (ValidationUtils.isBlank(eventType)) {
			return null;
		}

		return UPPERCASE_CACHE.computeIfAbsent(eventType, String::toUpperCase);
	}

	public byte[] eventTypeBytes() {

		if (ValidationUtils.isBlank(eventType)) {
			return null;
		}

		return BYTES.computeIfAbsent(eventType, String::getBytes);
	}

	public byte[] contentTypeBytes() {

		if (ValidationUtils.isBlank(contentType)) {
			return null;
		}

		return BYTES.computeIfAbsent(contentType, String::getBytes);
	}

	public String resourceTypeLowerCase() {

		if (ValidationUtils.isBlank(resourceType)) {
			return null;
		}

		return LOWERCASE_CACHE.computeIfAbsent(resourceType, String::toLowerCase);
	}

	public String resourceTypeUpperCase() {

		if (ValidationUtils.isBlank(resourceType)) {
			return null;
		}

		return UPPERCASE_CACHE.computeIfAbsent(resourceType, String::toUpperCase);
	}

	public String operationTypeLowerCase() {

		if (ValidationUtils.isBlank(operationType)) {
			return null;
		}

		return LOWERCASE_CACHE.computeIfAbsent(operationType, String::toLowerCase);
	}

	public String operationTypeUpperCase() {

		if (ValidationUtils.isBlank(operationType)) {
			return null;
		}

		return UPPERCASE_CACHE.computeIfAbsent(operationType, String::toUpperCase);
	}

	public String resultUpperCase() {

		if (ValidationUtils.isBlank(result)) {
			return null;
		}

		return UPPERCASE_CACHE.computeIfAbsent(result, String::toUpperCase);
	}

	public String resultLowerCase() {

		if (ValidationUtils.isBlank(result)) {
			return null;
		}

		return LOWERCASE_CACHE.computeIfAbsent(result, String::toLowerCase);
	}

	// kebab-case

	public String kindKebabCase() {

		if (ValidationUtils.isBlank(kind)) {
			return null;
		}

		return KEBAB_CACHE.computeIfAbsent(kind, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, v));
	}

	public String realmKebabCase() {

		if (ValidationUtils.isBlank(realm)) {
			return null;
		}

		return KEBAB_CACHE.computeIfAbsent(realm, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, v));
	}

	public String eventTypeKebabCase() {

		if (ValidationUtils.isBlank(eventType)) {
			return null;
		}

		return KEBAB_CACHE.computeIfAbsent(eventType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, v));
	}

	public String resourceTypeKebabCase() {

		if (ValidationUtils.isBlank(resourceType)) {
			return null;
		}

		return KEBAB_CACHE.computeIfAbsent(resourceType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, v));
	}

	public String operationTypeKebabCase() {

		if (ValidationUtils.isBlank(operationType)) {
			return null;
		}

		return KEBAB_CACHE.computeIfAbsent(operationType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, v));
	}

	public String resultKebabCase() {

		if (ValidationUtils.isBlank(result)) {
			return null;
		}

		return KEBAB_CACHE.computeIfAbsent(result, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, v));
	}

	// PascalCase

	public String kindPascalCase() {

		if (ValidationUtils.isBlank(kind)) {
			return null;
		}

		return PASCAL_CACHE.computeIfAbsent(kind, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, v));
	}

	public String realmPascalCase() {

		if (ValidationUtils.isBlank(realm)) {
			return null;
		}

		return PASCAL_CACHE.computeIfAbsent(realm, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, v));
	}

	public String eventTypePascalCase() {

		if (ValidationUtils.isBlank(eventType)) {
			return null;
		}

		return PASCAL_CACHE.computeIfAbsent(eventType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, v));
	}

	public String resourceTypePascalCase() {

		if (ValidationUtils.isBlank(resourceType)) {
			return null;
		}

		return PASCAL_CACHE.computeIfAbsent(resourceType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, v));
	}

	public String operationTypePascalCase() {

		if (ValidationUtils.isBlank(operationType)) {
			return null;
		}

		return PASCAL_CACHE.computeIfAbsent(operationType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, v));
	}

	public String resultPascalCase() {

		if (ValidationUtils.isBlank(result)) {
			return null;
		}

		return PASCAL_CACHE.computeIfAbsent(result, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, v));
	}

	// camelCase

	public String kindCamelCase() {

		if (ValidationUtils.isBlank(kind)) {
			return null;
		}

		return CAMEL_CACHE.computeIfAbsent(kind, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, v));
	}

	public String realmCamelCase() {

		if (ValidationUtils.isBlank(realm)) {
			return null;
		}

		return CAMEL_CACHE.computeIfAbsent(realm, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, v));
	}

	public String eventTypeCamelCase() {

		if (ValidationUtils.isBlank(eventType)) {
			return null;
		}

		return CAMEL_CACHE.computeIfAbsent(eventType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, v));
	}

	public String resourceTypeCamelCase() {

		if (ValidationUtils.isBlank(resourceType)) {
			return null;
		}

		return CAMEL_CACHE.computeIfAbsent(resourceType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, v));
	}

	public String operationTypeCamelCase() {

		if (ValidationUtils.isBlank(operationType)) {
			return null;
		}

		return CAMEL_CACHE.computeIfAbsent(operationType, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, v));
	}

	public String resultCamelCase() {

		if (ValidationUtils.isBlank(result)) {
			return null;
		}

		return CAMEL_CACHE.computeIfAbsent(result, v -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, v));
	}

	public static void clearCache() {
		BYTES.clear();
		LOWERCASE_CACHE.clear();
		UPPERCASE_CACHE.clear();
		KEBAB_CACHE.clear();
		PASCAL_CACHE.clear();
		CAMEL_CACHE.clear();
	}
}
