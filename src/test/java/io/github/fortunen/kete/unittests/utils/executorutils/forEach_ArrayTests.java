package io.github.fortunen.kete.unittests.utils.executorutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.fortunen.kete.utils.ExecutorUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;

public class forEach_ArrayTests {

	@Test
	public void shouldThrowWhenExecutorIsNull() {

		// arrange

		String[] items = {"a", "b", "c"};

		// act & assert

		assertThatThrownBy(() -> ExecutorUtils.forEach(null, items, item -> {}))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("executor is required");
	}

	@Test
	public void shouldThrowWhenItemsIsNull() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();

		try {

			// act & assert

			assertThatThrownBy(() -> ExecutorUtils.forEach(executor, (String[]) null, item -> {}))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("items is required");

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}

	@Test
	public void shouldThrowWhenActionIsNull() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		String[] items = {"a", "b", "c"};

		try {

			// act & assert

			assertThatThrownBy(() -> ExecutorUtils.forEach(executor, items, null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("action is required");

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}

	@Test
	public void shouldExecuteActionForEachItem() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		String[] items = {"a", "b", "c"};
		var processed = ConcurrentHashMap.newKeySet();

		try {

			// act

			ExecutorUtils.forEach(executor, items, processed::add);

			// assert

			assertThat(processed).containsExactlyInAnyOrder("a", "b", "c");

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}

	@Test
	public void shouldHandleEmptyArray() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		String[] items = {};
		var processed = ConcurrentHashMap.newKeySet();

		try {

			// act

			ExecutorUtils.forEach(executor, items, processed::add);

			// assert

			assertThat(processed).isEmpty();

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}

	@Test
	public void shouldBlockUntilAllItemsProcessed() {

		// arrange

		ExecutorService executor = ExecutorUtils.createService();
		Integer[] items = {1, 2, 3, 4, 5};
		var processed = ConcurrentHashMap.newKeySet();

		try {

			// act

			ExecutorUtils.forEach(executor, items, item -> {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				processed.add(item);
			});

			// assert

			assertThat(processed).hasSize(5);
			assertThat(processed).containsExactlyInAnyOrder(1, 2, 3, 4, 5);

		} finally {

			// cleanup

			executor.shutdownNow();
		}
	}
}
