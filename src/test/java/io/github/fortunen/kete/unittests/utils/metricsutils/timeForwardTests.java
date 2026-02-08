package io.github.fortunen.kete.unittests.utils.metricsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.utils.MetricsUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class timeForwardTests {

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
	public void shouldExecuteOperation() {

		// arrange

		var executed = new AtomicBoolean(false);

		// act

		MetricsUtils.timeForward("my-route", () -> executed.set(true));

		// assert

		assertThat(executed.get())
			.as("Should execute the operation")
			.isTrue();
	}

	@Test
	public void shouldRecordTimerMetric() {

		// act

		MetricsUtils.timeForward("timed-route", () -> {
			// simulate work
		});

		// assert

		var timer = meterRegistry.find(Constants.ID + ".forward.duration.seconds")
			.tag("route", "timed-route")
			.timer();

		assertThat(timer).isNotNull();
		assertThat(timer.count()).isEqualTo(1);
	}

	@Test
	public void shouldRecordMultipleTimings() {

		// act

		MetricsUtils.timeForward("multi-route", () -> {});
		MetricsUtils.timeForward("multi-route", () -> {});
		MetricsUtils.timeForward("multi-route", () -> {});

		// assert

		var timer = meterRegistry.find(Constants.ID + ".forward.duration.seconds")
			.tag("route", "multi-route")
			.timer();

		assertThat(timer).isNotNull();
		assertThat(timer.count()).isEqualTo(3);
	}

	@Test
	public void shouldTrackDifferentRoutesSeparately() {

		// act

		MetricsUtils.timeForward("route-a", () -> {});
		MetricsUtils.timeForward("route-b", () -> {});

		// assert

		var timerA = meterRegistry.find(Constants.ID + ".forward.duration.seconds")
			.tag("route", "route-a")
			.timer();

		var timerB = meterRegistry.find(Constants.ID + ".forward.duration.seconds")
			.tag("route", "route-b")
			.timer();

		assertThat(timerA).isNotNull();
		assertThat(timerA.count()).isEqualTo(1);
		assertThat(timerB).isNotNull();
		assertThat(timerB.count()).isEqualTo(1);
	}

	@Test
	public void shouldRecordNonZeroDuration() throws InterruptedException {

		// act

		MetricsUtils.timeForward("slow-route", () -> {
			try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		});

		// assert

		var timer = meterRegistry.find(Constants.ID + ".forward.duration.seconds")
			.tag("route", "slow-route")
			.timer();

		assertThat(timer).isNotNull();
		assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
			.as("Should record non-zero duration")
			.isGreaterThan(0);
	}

	@Test
	public void shouldStillExecuteOperationWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);
		var executed = new AtomicBoolean(false);

		// act

		MetricsUtils.timeForward("disabled-route", () -> executed.set(true));

		// assert

		assertThat(executed.get())
			.as("Operation should still execute when metrics disabled")
			.isTrue();
	}

	@Test
	public void shouldNotRecordTimerWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);

		// act

		MetricsUtils.timeForward("disabled-route", () -> {});

		// assert

		var timer = meterRegistry.find(Constants.ID + ".forward.duration.seconds")
			.timer();

		assertThat(timer).isNull();
	}

	@Test
	public void shouldPropagateExceptionFromOperation() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.timeForward("error-route", () -> {
			throw new RuntimeException("test error");
		}));

		// assert

		assertThat(thrown)
			.isInstanceOf(RuntimeException.class)
			.hasMessage("test error");
	}

	@Test
	public void shouldThrowForNullRoute() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.timeForward(null, () -> {}));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("route is required");
	}

	@Test
	public void shouldThrowForNullOperation() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.timeForward("my-route", null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("operation is required");
	}
}
