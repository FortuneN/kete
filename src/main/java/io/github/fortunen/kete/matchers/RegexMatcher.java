package io.github.fortunen.kete.matchers;

import java.util.regex.Pattern;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Component(name = "regex")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class RegexMatcher extends Matcher {

	private Pattern regex;

	@Override
	public void initialize() {

		ValidationUtils.requireNonNull(pattern, "pattern is required");

		regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean matches(String eventType) {

		ValidationUtils.requireNonNull(eventType, "eventType is required");
		ValidationUtils.requireNonNull(regex, "regex is required");

		return regex.matcher(eventType).matches();
	}
}
