package io.github.fortunen.kete.unittests.utils.executorutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ExecutorUtils;
import org.junit.jupiter.api.Test;

public class createServiceTests {

	@Test
	public void shouldReturnNonNullExecutorService() {

		// act

		var executor = ExecutorUtils.createService();

		// assert

		assertThat(executor).isNotNull();

		// cleanup

		executor.shutdownNow();
	}

	@Test
	public void shouldReturnExecutorThatIsNotShutdown() {

		// act

		var executor = ExecutorUtils.createService();

		// assert

		assertThat(executor.isShutdown()).isFalse();
		assertThat(executor.isTerminated()).isFalse();

		// cleanup

		executor.shutdownNow();
	}

	@Test
	public void shouldReturnExecutorThatCanExecuteTasks() throws Exception {

		// arrange

		var executor = ExecutorUtils.createService();
		var taskExecuted = new java.util.concurrent.atomic.AtomicBoolean(false);

		// act

		executor.submit(() -> taskExecuted.set(true)).get();

		// assert

		assertThat(taskExecuted.get()).isTrue();

		// cleanup

		executor.shutdownNow();
	}
}
