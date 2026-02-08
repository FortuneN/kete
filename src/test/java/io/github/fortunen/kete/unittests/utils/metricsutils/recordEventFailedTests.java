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

public class recordEventFailedTests {

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
	public void shouldIncrementFailedCounter() {

		// act

		MetricsUtils.recordEventFailed("kafka-route", "LOGIN", "master", "IOException");

		// assert

		var counter = meterRegistry.find(Constants.ID + ".events.failed.total")
			.tag("route", "kafka-route")
			.tag("event_type", "LOGIN")
			.tag("realm", "master")
			.tag("error_type", "IOException")
			.counter();

		assertThat(counter).isNotNull();
		assertThat(counter.count()).isEqualTo(1.0);
	}

	@Test
	public void shouldIncrementMultipleTimes() {

		// act

		MetricsUtils.recordEventFailed("my-route", "LOGIN", "master", "TimeoutException");
		MetricsUtils.recordEventFailed("my-route", "LOGIN", "master", "TimeoutException");

		// assert

		var counter = meterRegistry.find(Constants.ID + ".events.failed.total")
			.tag("route", "my-route")
			.tag("error_type", "TimeoutException")
			.counter();

		assertThat(counter).isNotNull();
		assertThat(counter.count()).isEqualTo(2.0);
	}

	@Test
	public void shouldTrackDifferentErrorTypesSeparately() {

		// act

		MetricsUtils.recordEventFailed("my-route", "LOGIN", "master", "IOException");
		MetricsUtils.recordEventFailed("my-route", "LOGIN", "master", "TimeoutException");

		// assert

		var ioCounter = meterRegistry.find(Constants.ID + ".events.failed.total")
			.tag("error_type", "IOException")
			.counter();

		var timeoutCounter = meterRegistry.find(Constants.ID + ".events.failed.total")
			.tag("error_type", "TimeoutException")
			.counter();

		assertThat(ioCounter).isNotNull();
		assertThat(ioCounter.count()).isEqualTo(1.0);
		assertThat(timeoutCounter).isNotNull();
		assertThat(timeoutCounter.count()).isEqualTo(1.0);
	}

	@Test
	public void shouldTrackDifferentRoutesSeparately() {

		// act

		MetricsUtils.recordEventFailed("route-a", "LOGIN", "master", "IOException");
		MetricsUtils.recordEventFailed("route-b", "LOGIN", "master", "IOException");

		// assert

		var counterA = meterRegistry.find(Constants.ID + ".events.failed.total")
			.tag("route", "route-a")
			.counter();

		var counterB = meterRegistry.find(Constants.ID + ".events.failed.total")
			.tag("route", "route-b")
			.counter();

		assertThat(counterA).isNotNull();
		assertThat(counterA.count()).isEqualTo(1.0);
		assertThat(counterB).isNotNull();
		assertThat(counterB.count()).isEqualTo(1.0);
	}

	@Test
	public void shouldNotRecordWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);

		// act

		MetricsUtils.recordEventFailed("my-route", "LOGIN", "master", "IOException");

		// assert

		var counter = meterRegistry.find(Constants.ID + ".events.failed.total")
			.counter();

		assertThat(counter).isNull();
	}

	@Test
	public void shouldThrowForNullRoute() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordEventFailed(null, "LOGIN", "master", "IOException"));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("route is required");
	}

	@Test
	public void shouldThrowForNullEventType() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordEventFailed("my-route", null, "master", "IOException"));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("eventType is required");
	}

	@Test
	public void shouldThrowForNullRealm() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordEventFailed("my-route", "LOGIN", null, "IOException"));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("realm is required");
	}

	@Test
	public void shouldThrowForNullErrorType() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordEventFailed("my-route", "LOGIN", "master", null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("errorType is required");
	}
}
