package io.github.fortunen.kete.matchers;

import com.hrakaroo.glob.GlobPattern;
import com.hrakaroo.glob.MatchingEngine;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Matcher;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Component(name = "sql")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class SqlMatcher extends Matcher {

	private MatchingEngine matcher;

	@Override
	public void initialize() {

		ValidationUtils.requireNonNull(pattern, "pattern is required");

		matcher = GlobPattern.compile(pattern, '%',  '_', GlobPattern.CASE_INSENSITIVE | GlobPattern.HANDLE_ESCAPES);
	}

	@Override
	public boolean matches(String eventType) {

		ValidationUtils.requireNonNull(eventType, "eventType is required");
		ValidationUtils.requireNonNull(matcher, "matcher is required");

		return matcher.matches(eventType);
	}
}
