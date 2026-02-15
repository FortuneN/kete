package io.github.fortunen.kete.utils;

import org.apache.commons.pool2.impl.GenericObjectPool;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

public final class MetricsUtils {

	private MetricsUtils() {}

	private static boolean enabled = false;

	public static void configure(boolean enabled) {

		MetricsUtils.enabled = enabled;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void recordEventForwarded(String route, String eventType, String realm) {

		ValidationUtils.requireNonNull(route, "route is required");
		ValidationUtils.requireNonNull(eventType, "eventType is required");
		ValidationUtils.requireNonNull(realm, "realm is required");

		if (!enabled) {
			return;
		}

		Metrics.counter(Constants.ID + ".events.forwarded.total", "route", route, "event_type", eventType, "realm", realm).increment();
	}

	public static void recordEventFailed(String route, String eventType, String realm, String errorType) {

		ValidationUtils.requireNonNull(route, "route is required");
		ValidationUtils.requireNonNull(eventType, "eventType is required");
		ValidationUtils.requireNonNull(realm, "realm is required");
		ValidationUtils.requireNonNull(errorType, "errorType is required");

		if (!enabled) {
			return;
		}

		Metrics.counter(Constants.ID + ".events.failed.total", "route", route, "event_type", eventType, "realm", realm, "error_type", errorType).increment();
	}

	@SuppressWarnings("null")
	public static void timeForward(String route, Runnable operation) {

		ValidationUtils.requireNonNull(route, "route is required");
		ValidationUtils.requireNonNull(operation, "operation is required");

		Timer.Sample sample = null;

		if (enabled) {
			sample = Timer.start();
	 	}

		operation.run();

		if (ValidationUtils.isNull(sample)) {
			return;
		}

		sample.stop(Metrics.timer(Constants.ID + ".forward.duration.seconds", "route", route));
	}

	public static void recordActiveRoutes(int count) {

		ValidationUtils.requireNonNegative(count, "count must be non-negative");

		if (!enabled) {
			return;
		}

		Metrics.gauge(Constants.ID + ".routes.active", count);
	}

	public static void registerPoolMetrics(String route, GenericObjectPool<Destination<?>> pool) {

		ValidationUtils.requireNonBlank(route, "route is required");
		ValidationUtils.requireNonNull(pool, "pool is required");

		if (!enabled) {
			return;
		}

		var tags = Tags.of("route", route);

		Metrics.gauge(Constants.ID + ".pool.idle", tags, pool, GenericObjectPool::getNumIdle);
		Metrics.gauge(Constants.ID + ".pool.active", tags, pool, GenericObjectPool::getNumActive);
		Metrics.gauge(Constants.ID + ".pool.total", tags, pool, GenericObjectPool::getMaxTotal);
	}
}
