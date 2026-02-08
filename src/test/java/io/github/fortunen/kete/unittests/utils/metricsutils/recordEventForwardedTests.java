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

public class recordEventForwardedTests {

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
	public void shouldIncrementForwardedCounter() {

		// act

		MetricsUtils.recordEventForwarded("kafka-route", "LOGIN", "master");

		// assert

		var counter = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("route", "kafka-route")
			.tag("event_type", "LOGIN")
			.tag("realm", "master")
			.counter();

		assertThat(counter).isNotNull();
		assertThat(counter.count()).isEqualTo(1.0);
	}

	@Test
	public void shouldIncrementMultipleTimes() {

		// act

		MetricsUtils.recordEventForwarded("my-route", "LOGIN", "realm-a");
		MetricsUtils.recordEventForwarded("my-route", "LOGIN", "realm-a");
		MetricsUtils.recordEventForwarded("my-route", "LOGIN", "realm-a");

		// assert

		var counter = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("route", "my-route")
			.tag("event_type", "LOGIN")
			.tag("realm", "realm-a")
			.counter();

		assertThat(counter).isNotNull();
		assertThat(counter.count()).isEqualTo(3.0);
	}

	@Test
	public void shouldTrackDifferentRoutesSeparately() {

		// act

		MetricsUtils.recordEventForwarded("route-a", "LOGIN", "master");
		MetricsUtils.recordEventForwarded("route-b", "LOGIN", "master");

		// assert

		var counterA = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("route", "route-a")
			.counter();

		var counterB = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("route", "route-b")
			.counter();

		assertThat(counterA).isNotNull();
		assertThat(counterA.count()).isEqualTo(1.0);
		assertThat(counterB).isNotNull();
		assertThat(counterB.count()).isEqualTo(1.0);
	}

	@Test
	public void shouldTrackDifferentEventTypesSeparately() {

		// act

		MetricsUtils.recordEventForwarded("my-route", "LOGIN", "master");
		MetricsUtils.recordEventForwarded("my-route", "LOGOUT", "master");

		// assert

		var loginCounter = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("event_type", "LOGIN")
			.counter();

		var logoutCounter = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("event_type", "LOGOUT")
			.counter();

		assertThat(loginCounter).isNotNull();
		assertThat(loginCounter.count()).isEqualTo(1.0);
		assertThat(logoutCounter).isNotNull();
		assertThat(logoutCounter.count()).isEqualTo(1.0);
	}

	@Test
	public void shouldTrackDifferentRealmsSeparately() {

		// act

		MetricsUtils.recordEventForwarded("my-route", "LOGIN", "realm-a");
		MetricsUtils.recordEventForwarded("my-route", "LOGIN", "realm-b");

		// assert

		var realmA = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("realm", "realm-a")
			.counter();

		var realmB = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.tag("realm", "realm-b")
			.counter();

		assertThat(realmA).isNotNull();
		assertThat(realmA.count()).isEqualTo(1.0);
		assertThat(realmB).isNotNull();
		assertThat(realmB.count()).isEqualTo(1.0);
	}

	@Test
	public void shouldNotRecordWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);

		// act

		MetricsUtils.recordEventForwarded("my-route", "LOGIN", "master");

		// assert

		var counter = meterRegistry.find(Constants.ID + ".events.forwarded.total")
			.counter();

		assertThat(counter).isNull();
	}

	@Test
	public void shouldThrowForNullRoute() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordEventForwarded(null, "LOGIN", "master"));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("route is required");
	}

	@Test
	public void shouldThrowForNullEventType() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordEventForwarded("my-route", null, "master"));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("eventType is required");
	}

	@Test
	public void shouldThrowForNullRealm() {

		// act

		var thrown = catchThrowable(() -> MetricsUtils.recordEventForwarded("my-route", "LOGIN", null));

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("realm is required");
	}
}
