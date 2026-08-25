package io.github.fortunen.kete.unittests.utils.metricsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.utils.MetricsUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class recordRetryTests {

	private SimpleMeterRegistry meterRegistry;

	@BeforeEach
	void setUp() {
		Metrics.globalRegistry.forEachMeter(Metrics.globalRegistry::remove);
		meterRegistry = new SimpleMeterRegistry();
		Metrics.globalRegistry.add(meterRegistry);
		MetricsUtils.configure(true);
	}

	@AfterEach
	void tearDown() {
		Metrics.globalRegistry.remove(meterRegistry);
		meterRegistry.close();
		MetricsUtils.configure(false);
	}

	@Test
	public void shouldCountRetriesPerRoute() {

		// act

		MetricsUtils.recordRetry("route-a");
		MetricsUtils.recordRetry("route-a");
		MetricsUtils.recordRetry("route-b");

		// assert

		assertThat(meterRegistry.find(Constants.ID + ".retries.total").tag("route", "route-a").counter().count()).isEqualTo(2.0);
		assertThat(meterRegistry.find(Constants.ID + ".retries.total").tag("route", "route-b").counter().count()).isEqualTo(1.0);
	}

	@Test
	public void shouldNotRecordWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);

		// act

		MetricsUtils.recordRetry("route-a");

		// assert

		assertThat(meterRegistry.find(Constants.ID + ".retries.total").counter()).isNull();
	}

	@Test
	public void shouldThrowWhenRouteIsNull() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordRetry(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("route is required");
	}
}
