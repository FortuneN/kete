package io.github.fortunen.kete;

import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public abstract class Matcher {

	protected String name;
	protected boolean not;
	protected String pattern;

	public abstract void initialize();
	public abstract boolean matches(String eventType);

	public boolean accept(String eventType) {

		if (ValidationUtils.isBlank(eventType)) {
			return false;
		}

		var matches = matches(eventType);

		if (not) {
			return !matches;
		}

		return matches;
	}
}
