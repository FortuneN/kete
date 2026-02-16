package io.github.fortunen.kete.unittests.utils.awsutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.AwsUtils;

public class resolveRegionTests {

	// =========================================================================
	// Config region takes priority
	// =========================================================================

	@Test
	public void shouldReturnConfigRegionWhenSet() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("region", "eu-west-1");
		var configuration = new MapConfiguration(map);

		// act

		var result = AwsUtils.resolveRegion(configuration);

		// assert

		assertThat(result).isEqualTo("eu-west-1");
	}

	@Test
	public void shouldTrimConfigRegion() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("region", "  us-east-1  ");
		var configuration = new MapConfiguration(map);

		// act

		var result = AwsUtils.resolveRegion(configuration);

		// assert

		assertThat(result).isEqualTo("us-east-1");
	}

	// =========================================================================
	// Config region missing / blank — falls back to environment
	// =========================================================================

	@Test
	public void shouldFallBackToEnvironmentWhenConfigRegionIsEmpty() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("region", "");
		var configuration = new MapConfiguration(map);

		// act

		var result = AwsUtils.resolveRegion(configuration);

		// assert — result comes from AWS_REGION, AWS_DEFAULT_REGION, or null

		var envRegion = System.getenv("AWS_REGION");
		var envDefaultRegion = System.getenv("AWS_DEFAULT_REGION");

		if (envRegion != null && !envRegion.isBlank()) {
			assertThat(result).isEqualTo(envRegion);
		} else if (envDefaultRegion != null && !envDefaultRegion.isBlank()) {
			assertThat(result).isEqualTo(envDefaultRegion);
		} else {
			assertThat(result).isNull();
		}
	}

	@Test
	public void shouldFallBackToEnvironmentWhenConfigRegionIsBlank() {

		// arrange

		var map = new HashMap<String, Object>();
		map.put("region", "   ");
		var configuration = new MapConfiguration(map);

		// act

		var result = AwsUtils.resolveRegion(configuration);

		// assert — result comes from AWS_REGION, AWS_DEFAULT_REGION, or null

		var envRegion = System.getenv("AWS_REGION");
		var envDefaultRegion = System.getenv("AWS_DEFAULT_REGION");

		if (envRegion != null && !envRegion.isBlank()) {
			assertThat(result).isEqualTo(envRegion);
		} else if (envDefaultRegion != null && !envDefaultRegion.isBlank()) {
			assertThat(result).isEqualTo(envDefaultRegion);
		} else {
			assertThat(result).isNull();
		}
	}

	@Test
	public void shouldFallBackToEnvironmentWhenConfigRegionIsMissing() {

		// arrange

		var map = new HashMap<String, Object>();
		var configuration = new MapConfiguration(map);

		// act

		var result = AwsUtils.resolveRegion(configuration);

		// assert — result comes from AWS_REGION, AWS_DEFAULT_REGION, or null

		var envRegion = System.getenv("AWS_REGION");
		var envDefaultRegion = System.getenv("AWS_DEFAULT_REGION");

		if (envRegion != null && !envRegion.isBlank()) {
			assertThat(result).isEqualTo(envRegion);
		} else if (envDefaultRegion != null && !envDefaultRegion.isBlank()) {
			assertThat(result).isEqualTo(envDefaultRegion);
		} else {
			assertThat(result).isNull();
		}
	}
}
