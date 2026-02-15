package io.github.fortunen.kete.utils;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration2.MapConfiguration;

import io.github.fortunen.kete.Constants;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.SneakyThrows;

public final class RetryUtils {

	private RetryUtils() {}

	public static final String ENABLED = "enabled";
	public static final String MAX_ATTEMPTS = "max-attempts";
	public static final String WAIT_DURATION = "wait-duration";

	private static final AtomicInteger retryNumber = new AtomicInteger(1);

	@SneakyThrows
	public static Retry createRetry(MapConfiguration configuration) {

		ValidationUtils.requireNonNull(configuration, "configuration is required");

		if (!configuration.getBoolean(ENABLED, true)) {
			return null;
		}

		var builder = RetryConfig.custom().failAfterMaxAttempts(true);

		// maxAttempts

		var maxAttemptsString = configuration.getString(MAX_ATTEMPTS, "");

		if (ValidationUtils.isNotBlank(maxAttemptsString)) {
			ValidationUtils.tryParseInt(maxAttemptsString).ifPresentOrElse(maxAttempts -> {

				ValidationUtils.requireGreaterThan(maxAttempts, 0, MAX_ATTEMPTS + " must be greater than zero");
				builder.maxAttempts(maxAttempts);

			}, () -> {

				throw new IllegalStateException(MAX_ATTEMPTS + " must be an integer");

			});
		}

		// waitDuration

		var waitDurationString = configuration.getString(WAIT_DURATION, "");

		if (ValidationUtils.isNotBlank(waitDurationString)) {
			ValidationUtils.tryParseDuration(waitDurationString).ifPresentOrElse(waitDuration -> {

				ValidationUtils.requireFalse(waitDuration.isZero(), WAIT_DURATION + " must be greater than zero");
				builder.waitDuration(waitDuration);

			}, () -> {

				throw new IllegalStateException(WAIT_DURATION + " must be a duration");

			});
		}

		// return

		return Retry.of(Constants.ID + "-" + retryNumber.getAndIncrement(), builder.build());
	}
}
