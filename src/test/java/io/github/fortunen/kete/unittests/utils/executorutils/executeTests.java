package io.github.fortunen.kete.unittests.utils.executorutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.ExecutorUtils;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

public class executeTests {

	@Test
	public void shouldThrowWhenExecutorIsNull() {

		// act & assert

		assertThatThrownBy(() -> ExecutorUtils.execute(null, () -> {}))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("executor is required");
	}

	@Test
	public void shouldThrowWhenTaskIsNull() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();

		try {

			// act & assert

			assertThatThrownBy(() -> ExecutorUtils.execute(executor, null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("task is required");

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}

	@Test
	public void shouldExecuteTask() throws Exception {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		var taskExecuted = new AtomicBoolean(false);
		var latch = new CountDownLatch(1);

		try {

			// act

			ExecutorUtils.execute(executor, () -> {
				taskExecuted.set(true);
				latch.countDown();
			});

			// assert

			var completed = latch.await(5, TimeUnit.SECONDS);
			assertThat(completed).isTrue();
			assertThat(taskExecuted.get()).isTrue();

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}

	@Test
	public void shouldExecuteMultipleTasks() throws Exception {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		var counter = new java.util.concurrent.atomic.AtomicInteger(0);
		var latch = new CountDownLatch(3);

		try {

			// act

			ExecutorUtils.execute(executor, () -> { counter.incrementAndGet(); latch.countDown(); });
			ExecutorUtils.execute(executor, () -> { counter.incrementAndGet(); latch.countDown(); });
			ExecutorUtils.execute(executor, () -> { counter.incrementAndGet(); latch.countDown(); });

			// assert

			var completed = latch.await(5, TimeUnit.SECONDS);
			assertThat(completed).isTrue();
			assertThat(counter.get()).isEqualTo(3);

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}
}
