package io.github.fortunen.kete;

import java.util.List;

import io.github.fortunen.kete.utils.ValidationUtils;

public record SerializerRoutes(Serializer serializer, List<Route> routes) {

	public SerializerRoutes {
		ValidationUtils.requireNonNull(serializer, "serializer is required");
		ValidationUtils.requireNonNull(routes, "routes is required");
	}
}
