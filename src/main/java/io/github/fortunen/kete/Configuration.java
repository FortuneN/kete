package io.github.fortunen.kete;

import lombok.Data;

@Data
public class Configuration {

	private Route[] routes = new Route[0];
	private boolean metricsEnabled = false;
	private boolean supportTheProject = true;
}
