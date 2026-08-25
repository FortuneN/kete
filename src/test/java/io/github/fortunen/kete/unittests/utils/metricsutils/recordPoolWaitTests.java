package io.github.fortunen.kete.unittests.utils.metricsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.utils.MetricsUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class recordPoolWaitTests {

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
	public void shouldRecordWaitDurationPerRoute() {

		// act

		MetricsUtils.recordPoolWait("route-a", Duration.ofMillis(250));
		MetricsUtils.recordPoolWait("route-a", Duration.ofMillis(750));

		// assert

		var timer = meterRegistry.find(Constants.ID + ".pool.wait.seconds").tag("route", "route-a").timer();

		assertThat(timer).isNotNull();
		assertThat(timer.count()).isEqualTo(2);
		assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(1000.0);
	}

	@Test
	public void shouldNotRecordWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);

		// act

		MetricsUtils.recordPoolWait("route-a", Duration.ofMillis(5));

		// assert

		assertThat(meterRegistry.find(Constants.ID + ".pool.wait.seconds").timer()).isNull();
	}

	@Test
	public void shouldThrowWhenDurationIsNull() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordPoolWait("route-a", null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("duration is required");
	}
}
