package io.github.fortunen.kete;

import java.util.List;

public record SerializerRoutes(Serializer serializer, List<Route> routes) {}
