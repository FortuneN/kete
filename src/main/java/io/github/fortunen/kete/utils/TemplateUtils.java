package io.github.fortunen.kete.utils;

import java.util.HashMap;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.github.fortunen.kete.EventMessage;
import lombok.SneakyThrows;

public final class TemplateUtils {

	private TemplateUtils() {}

	public static final int TEMPLATE_CACHE_MAX_SIZE = 10000;

	private static Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(TEMPLATE_CACHE_MAX_SIZE).build();

	@SneakyThrows
	public static String substitute(String template, EventMessage message) {

		if (ValidationUtils.isBlank(template)) {
			return template;
		}

		if (ValidationUtils.isNull(message)) {
			return template;
		}

		var cacheKey = new StringBuilder()
			.append(template).append('\0')
			.append(message.kind()).append('\0')
			.append(message.realm()).append('\0')
			.append(message.eventType()).append('\0')
			.append(message.resourceType()).append('\0')
			.append(message.operationType()).append('\0')
			.append(message.result()).toString();

		return cache.get(cacheKey, () -> {

			var values = new HashMap<String, String>();

			if (ValidationUtils.isNotBlank(message.kind())) {
				values.put("kindUpperCase", message.kindUpperCase());
				values.put("kindLowerCase", message.kindLowerCase());
			}

			if (ValidationUtils.isNotBlank(message.realm())) {
				values.put("realmUpperCase", message.realmUpperCase());
				values.put("realmLowerCase", message.realmLowerCase());
			}

			if (ValidationUtils.isNotBlank(message.eventType())) {
				values.put("eventTypeUpperCase", message.eventTypeUpperCase());
				values.put("eventTypeLowerCase", message.eventTypeLowerCase());
			}

			if (ValidationUtils.isNotBlank(message.resourceType())) {
				values.put("resourceTypeUpperCase", message.resourceTypeUpperCase());
				values.put("resourceTypeLowerCase", message.resourceTypeLowerCase());
			}

			if (ValidationUtils.isNotBlank(message.operationType())) {
				values.put("operationTypeUpperCase", message.operationTypeUpperCase());
				values.put("operationTypeLowerCase", message.operationTypeLowerCase());
			}

			if (ValidationUtils.isNotBlank(message.result())) {
				values.put("resultUpperCase", message.resultUpperCase());
				values.put("resultLowerCase", message.resultLowerCase());
			}

			var substitutor = new StringSubstitutor(values, "${", "}");
			substitutor.setEnableUndefinedVariableException(false);

			return substitutor.replace(template);
		});
	}

	public static void clearCache() {
		cache.invalidateAll();
	}
}
