package io.github.fortunen.kete;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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

	public String kindLowerCase() {
		return LOWERCASE_CACHE.computeIfAbsent(kind, String::toLowerCase);
	}

	public String kindUpperCase() {
		return UPPERCASE_CACHE.computeIfAbsent(kind, String::toUpperCase);
	}

	public byte[] kindBytes() {
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

	public static void clearCache() {
		BYTES.clear();
		LOWERCASE_CACHE.clear();
		UPPERCASE_CACHE.clear();
	}
}
