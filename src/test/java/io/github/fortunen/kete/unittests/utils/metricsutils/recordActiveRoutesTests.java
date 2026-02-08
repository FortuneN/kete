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

public class recordActiveRoutesTests {

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
	public void shouldRecordRouteCount() {

		// act

		MetricsUtils.recordActiveRoutes(5);

		// assert

		var gauge = meterRegistry.find(Constants.ID + ".routes.active")
			.gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(5.0);
	}

	@Test
	public void shouldRecordZeroRoutes() {

		// act

		MetricsUtils.recordActiveRoutes(0);

		// assert

		var gauge = meterRegistry.find(Constants.ID + ".routes.active")
			.gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(0.0);
	}

	@Test
	public void shouldRecordLargeCount() {

		// act

		MetricsUtils.recordActiveRoutes(100);

		// assert

		var gauge = meterRegistry.find(Constants.ID + ".routes.active")
			.gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(100.0);
	}

	@Test
	public void shouldNotRecordWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);

		// act

		MetricsUtils.recordActiveRoutes(3);

		// assert

		var gauge = meterRegistry.find(Constants.ID + ".routes.active")
			.gauge();

		assertThat(gauge).isNull();
	}

	@Test
	public void shouldThrowForNegativeCount() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordActiveRoutes(-1));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("count must be non-negative");
	}

	@Test
	public void shouldThrowForLargeNegativeCount() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordActiveRoutes(-100));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("count must be non-negative");
	}
}
