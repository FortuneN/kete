package io.github.fortunen.kete.unittests.utils.executorutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.fortunen.kete.utils.ExecutorUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

public class shutdown_TimeoutTests {

	@Test
	public void shouldNotThrowWhenExecutorServiceIsNull() {

		// act & assert

		assertThatCode(() -> ExecutorUtils.shutdown(null, 5, "test"))
			.doesNotThrowAnyException();
	}

	@Test
	public void shouldShutdownExecutorService() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();

		// act

		ExecutorUtils.shutdown(executor, 5, "test executor");

		// assert

		assertThat(executor.isShutdown()).isTrue();
	}

	@Test
	public void shouldTerminateExecutorService() throws Exception {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();

		// act

		ExecutorUtils.shutdown(executor, 5, "test executor");

		// assert

		assertThat(executor.isTerminated()).isTrue();
	}

	@Test
	public void shouldHandleAlreadyShutdownExecutor() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		executor.shutdownNow();

		// act & assert

		assertThatCode(() -> ExecutorUtils.shutdown(executor, 5, "already shutdown"))
			.doesNotThrowAnyException();
	}

	@Test
	public void shouldWaitForRunningTasksToComplete() throws Exception {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		var taskCompleted = new AtomicBoolean(false);

		executor.submit(() -> {
			try {
				Thread.sleep(100);
				taskCompleted.set(true);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		// act

		ExecutorUtils.shutdown(executor, 5, "test executor");

		// assert

		assertThat(executor.isTerminated()).isTrue();
	}
}
