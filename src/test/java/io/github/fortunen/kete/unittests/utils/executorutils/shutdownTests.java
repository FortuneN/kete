package io.github.fortunen.kete.unittests.utils.executorutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.fortunen.kete.utils.ExecutorUtils;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;

public class shutdownTests {

	@Test
	public void shouldNotThrowWhenExecutorServiceIsNull() {

		// act & assert

		assertThatCode(() -> ExecutorUtils.shutdown(null, "test"))
			.doesNotThrowAnyException();
	}

	@Test
	public void shouldShutdownExecutorService() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();

		// act

		ExecutorUtils.shutdown(executor, "test executor");

		// assert

		assertThat(executor.isShutdown()).isTrue();
	}

	@Test
	public void shouldTerminateExecutorService() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();

		// act

		ExecutorUtils.shutdown(executor, "test executor");

		// assert

		assertThat(executor.isTerminated()).isTrue();
	}

	@Test
	public void shouldHandleAlreadyShutdownExecutor() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		executor.shutdownNow();

		// act & assert

		assertThatCode(() -> ExecutorUtils.shutdown(executor, "already shutdown"))
			.doesNotThrowAnyException();
	}

	@Test
	public void shouldUseDefaultTimeoutOf30Seconds() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();

		// act

		ExecutorUtils.shutdown(executor, "test executor");

		// assert

		assertThat(executor.isTerminated()).isTrue();
	}
}
