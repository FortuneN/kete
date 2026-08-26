package io.github.fortunen.kete.utils;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class LogThrottle {

	private final long windowNanos;
	private final LongSupplier nanoClock;
	private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

	private static final class State {

		private long windowStart;
		private long suppressed;

		private State(long windowStart) {
			this.windowStart = windowStart;
		}
	}

	public LogThrottle(Duration window) {
		this(window, System::nanoTime);
	}

	public LogThrottle(Duration window, LongSupplier nanoClock) {

		ValidationUtils.requireNonNull(window, "window is required");
		ValidationUtils.requireNonNull(nanoClock, "nanoClock is required");
		ValidationUtils.requireFalse(window.isNegative() || window.isZero(), "window must be positive");

		this.windowNanos = window.toNanos();
		this.nanoClock = nanoClock;
	}

	// Returns -1 when the caller must stay silent; otherwise the number of occurrences that were
	// suppressed since the last permitted call for this key (the first call for a key is always permitted).

	public long permit(String key) {

		ValidationUtils.requireNonNull(key, "key is required");

		var now = nanoClock.getAsLong();
		var state = states.computeIfAbsent(key, k -> new State(now - windowNanos));

		synchronized (state) {

			if (now - state.windowStart >= windowNanos) {
				var suppressed = state.suppressed;
				state.windowStart = now;
				state.suppressed = 0;
				return suppressed;
			}

			state.suppressed++;

			return -1;
		}
	}
}
