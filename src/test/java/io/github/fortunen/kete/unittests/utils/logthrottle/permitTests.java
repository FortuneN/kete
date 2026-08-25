package io.github.fortunen.kete.unittests.utils.logthrottle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.LogThrottle;

public class permitTests {

	@Test
	public void shouldPermitFirstCallForKey() {

		// arrange

		var throttle = new LogThrottle(Duration.ofMinutes(1), () -> 0L);

		// act

		var suppressed = throttle.permit("route-a");

		// assert

		assertThat(suppressed).isZero();
	}

	@Test
	public void shouldSuppressCallsInsideWindow() {

		// arrange

		var clock = new AtomicLong(0);
		var throttle = new LogThrottle(Duration.ofMinutes(1), clock::get);
		throttle.permit("route-a");

		// act

		clock.set(Duration.ofSeconds(30).toNanos());
		var first = throttle.permit("route-a");
		var second = throttle.permit("route-a");

		// assert

		assertThat(first).isEqualTo(-1);
		assertThat(second).isEqualTo(-1);
	}

	@Test
	public void shouldReportSuppressedCountWhenWindowElapses() {

		// arrange

		var clock = new AtomicLong(0);
		var throttle = new LogThrottle(Duration.ofMinutes(1), clock::get);
		throttle.permit("route-a");
		throttle.permit("route-a");
		throttle.permit("route-a");
		throttle.permit("route-a");

		// act

		clock.set(Duration.ofMinutes(1).toNanos());
		var suppressed = throttle.permit("route-a");
		var next = throttle.permit("route-a");

		// assert

		assertThat(suppressed).isEqualTo(3);
		assertThat(next).isEqualTo(-1);
	}

	@Test
	public void shouldTrackKeysIndependently() {

		// arrange

		var throttle = new LogThrottle(Duration.ofMinutes(1), () -> 0L);
		throttle.permit("route-a");

		// act

		var other = throttle.permit("route-b");
		var same = throttle.permit("route-a");

		// assert

		assertThat(other).isZero();
		assertThat(same).isEqualTo(-1);
	}

	@Test
	public void shouldThrowWhenKeyIsNull() {

		// arrange

		var throttle = new LogThrottle(Duration.ofMinutes(1));

		// act

		var thrown = catchThrowable(() -> throttle.permit(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("key is required");
	}

	@Test
	public void shouldThrowWhenWindowIsNotPositive() {

		// act

		var thrown = catchThrowable(() -> new LogThrottle(Duration.ZERO));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("window must be positive");
	}
}
