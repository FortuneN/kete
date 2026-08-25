package io.github.fortunen.kete.unittests.utils.metricsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.utils.MetricsUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class registerInFlightTests {

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
	public void shouldExposeLiveInFlightCount() {

		// arrange

		var inFlight = new AtomicInteger();

		// act

		MetricsUtils.registerInFlight("route-a", inFlight);
		inFlight.set(3);

		// assert

		var gauge = meterRegistry.find(Constants.ID + ".events.inflight").tag("route", "route-a").gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(3.0);
	}

	@Test
	public void shouldNotRegisterWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);

		// act

		MetricsUtils.registerInFlight("route-a", new AtomicInteger());

		// assert

		assertThat(meterRegistry.find(Constants.ID + ".events.inflight").gauge()).isNull();
	}

	@Test
	public void shouldThrowWhenRouteIsBlank() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.registerInFlight(" ", new AtomicInteger()));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("route is required");
	}
}
