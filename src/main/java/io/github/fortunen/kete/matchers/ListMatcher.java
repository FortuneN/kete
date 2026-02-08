package io.github.fortunen.kete.matchers;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Component(name = "list")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class ListMatcher extends Matcher {

	private Set<String> eventTypes;

	@Override
	public void initialize() {

		ValidationUtils.requireNonNull(pattern, "pattern is required");

		eventTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

		Arrays
			.stream(StringUtils.split(pattern, ","))
			.map(String::trim)
			.filter(ValidationUtils::isNotBlank)
			.forEach(eventTypes::add);
	}

	@Override
	public boolean matches(String eventType) {

		ValidationUtils.requireNonNull(eventType, "eventType is required");
		ValidationUtils.requireNonNull(eventTypes, "eventTypes is required");

		return eventTypes.contains(eventType);
	}
}
