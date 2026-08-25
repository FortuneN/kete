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
				values.put("kindKebabCase", message.kindKebabCase());
				values.put("kindPascalCase", message.kindPascalCase());
				values.put("kindCamelCase", message.kindCamelCase());
			}

			if (ValidationUtils.isNotBlank(message.realm())) {
				values.put("realmUpperCase", message.realmUpperCase());
				values.put("realmLowerCase", message.realmLowerCase());
				values.put("realmKebabCase", message.realmKebabCase());
				values.put("realmPascalCase", message.realmPascalCase());
				values.put("realmCamelCase", message.realmCamelCase());
			}

			if (ValidationUtils.isNotBlank(message.eventType())) {
				values.put("eventTypeUpperCase", message.eventTypeUpperCase());
				values.put("eventTypeLowerCase", message.eventTypeLowerCase());
				values.put("eventTypeKebabCase", message.eventTypeKebabCase());
				values.put("eventTypePascalCase", message.eventTypePascalCase());
				values.put("eventTypeCamelCase", message.eventTypeCamelCase());
			}

			if (ValidationUtils.isNotBlank(message.resourceType())) {
				values.put("resourceTypeUpperCase", message.resourceTypeUpperCase());
				values.put("resourceTypeLowerCase", message.resourceTypeLowerCase());
				values.put("resourceTypeKebabCase", message.resourceTypeKebabCase());
				values.put("resourceTypePascalCase", message.resourceTypePascalCase());
				values.put("resourceTypeCamelCase", message.resourceTypeCamelCase());
			}

			if (ValidationUtils.isNotBlank(message.operationType())) {
				values.put("operationTypeUpperCase", message.operationTypeUpperCase());
				values.put("operationTypeLowerCase", message.operationTypeLowerCase());
				values.put("operationTypeKebabCase", message.operationTypeKebabCase());
				values.put("operationTypePascalCase", message.operationTypePascalCase());
				values.put("operationTypeCamelCase", message.operationTypeCamelCase());
			}

			if (ValidationUtils.isNotBlank(message.result())) {
				values.put("resultUpperCase", message.resultUpperCase());
				values.put("resultLowerCase", message.resultLowerCase());
				values.put("resultKebabCase", message.resultKebabCase());
				values.put("resultPascalCase", message.resultPascalCase());
				values.put("resultCamelCase", message.resultCamelCase());
			}

			var substitutor = new StringSubstitutor(values, "${", "}");
			substitutor.setEnableUndefinedVariableException(false);

			return substitutor.replace(template);
		});
	}

	public static String maskTemplates(String value) {
		return ValidationUtils.isNull(value) ? null : value.replaceAll("\\$\\{[^}]*\\}", "template");
	}

	public static boolean containsTemplate(String value) {
		return value != null && value.contains("${");
	}

	public static void clearCache() {
		cache.invalidateAll();
	}
}
