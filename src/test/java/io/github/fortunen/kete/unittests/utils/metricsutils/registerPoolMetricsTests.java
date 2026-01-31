package io.github.fortunen.kete.unittests.utils.metricsutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.utils.MetricsUtils;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class registerPoolMetricsTests {

	private SimpleMeterRegistry meterRegistry;

	@BeforeEach
	void setUp() {

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
	public void shouldThrowWhenRouteIsNull() {

		// arrange

		var pool = mock(GenericObjectPool.class);

		// act

		@SuppressWarnings("unchecked")
		var thrown = catchThrowable(() -> {
			MetricsUtils.registerPoolMetrics(null, pool);
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("route is required");
	}

	@Test
	public void shouldThrowWhenRouteIsEmpty() {

		// arrange

		var pool = mock(GenericObjectPool.class);

		// act

		@SuppressWarnings("unchecked")
		var thrown = catchThrowable(() -> {
			MetricsUtils.registerPoolMetrics("", pool);
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("route is required");
	}

	@Test
	public void shouldThrowWhenRouteIsBlank() {

		// arrange

		var pool = mock(GenericObjectPool.class);

		// act

		@SuppressWarnings("unchecked")
		var thrown = catchThrowable(() -> {
			MetricsUtils.registerPoolMetrics("   ", pool);
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("route is required");
	}

	@Test
	public void shouldThrowWhenPoolIsNull() {

		// act

		var thrown = catchThrowable(() -> {
			MetricsUtils.registerPoolMetrics("my-route", null);
		});

		// assert

		assertThat(thrown)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("pool is required");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldRegisterIdleGauge() {

		// arrange

		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		when(pool.getNumIdle()).thenReturn(5);

		// act

		MetricsUtils.registerPoolMetrics("idle-route", pool);

		// assert

		Gauge gauge = meterRegistry.find(Constants.ID + ".pool.idle")
			.tag("route", "idle-route")
			.gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(5.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldRegisterActiveGauge() {

		// arrange

		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		when(pool.getNumIdle()).thenReturn(0);
		when(pool.getNumActive()).thenReturn(3);
		when(pool.getMaxTotal()).thenReturn(0);

		// act

		MetricsUtils.registerPoolMetrics("active-route", pool);

		// assert

		Gauge gauge = meterRegistry.find(Constants.ID + ".pool.active")
			.tag("route", "active-route")
			.gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(3.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldRegisterTotalGauge() {

		// arrange

		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		when(pool.getNumIdle()).thenReturn(0);
		when(pool.getNumActive()).thenReturn(0);
		when(pool.getMaxTotal()).thenReturn(10);

		// act

		MetricsUtils.registerPoolMetrics("total-route", pool);

		// assert

		Gauge gauge = meterRegistry.find(Constants.ID + ".pool.total")
			.tag("route", "total-route")
			.gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(10.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldRegisterAllPoolMetricsWithRouteTag() {

		// arrange

		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		when(pool.getNumIdle()).thenReturn(2);
		when(pool.getNumActive()).thenReturn(4);
		when(pool.getMaxTotal()).thenReturn(8);

		// act

		MetricsUtils.registerPoolMetrics("kafka-route", pool);

		// assert

		var idleGauge = meterRegistry.find(Constants.ID + ".pool.idle")
			.tag("route", "kafka-route")
			.gauge();
		var activeGauge = meterRegistry.find(Constants.ID + ".pool.active")
			.tag("route", "kafka-route")
			.gauge();
		var totalGauge = meterRegistry.find(Constants.ID + ".pool.total")
			.tag("route", "kafka-route")
			.gauge();

		assertThat(idleGauge).isNotNull();
		assertThat(idleGauge.value()).isEqualTo(2.0);

		assertThat(activeGauge).isNotNull();
		assertThat(activeGauge.value()).isEqualTo(4.0);

		assertThat(totalGauge).isNotNull();
		assertThat(totalGauge.value()).isEqualTo(8.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldNotRegisterMetricsWhenDisabled() {

		// arrange

		MetricsUtils.configure(false);
		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);

		// act

		MetricsUtils.registerPoolMetrics("my-route", pool);

		// assert

		var gauge = meterRegistry.find(Constants.ID + ".pool.idle")
			.tag("route", "my-route")
			.gauge();

		assertThat(gauge).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldReadDynamicPoolValues() {

		// arrange

		var pool = (GenericObjectPool<Destination<?>>) mock(GenericObjectPool.class);
		when(pool.getNumIdle()).thenReturn(1);

		// act

		MetricsUtils.registerPoolMetrics("dynamic-route", pool);

		// assert - initial value

		var gauge = meterRegistry.find(Constants.ID + ".pool.idle")
			.tag("route", "dynamic-route")
			.gauge();

		assertThat(gauge).isNotNull();
		assertThat(gauge.value()).isEqualTo(1.0);

		// arrange - change value

		when(pool.getNumIdle()).thenReturn(7);

		// assert - gauge reflects new value

		assertThat(gauge.value()).isEqualTo(7.0);
	}
}
