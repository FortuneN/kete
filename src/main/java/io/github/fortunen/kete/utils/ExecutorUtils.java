package io.github.fortunen.kete.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExecutorUtils {

	private ExecutorUtils() {}

	public static ExecutorService createService() {

		// java 21+

		return Executors.newVirtualThreadPerTaskExecutor();

		// pre java 21

		// return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	}

	public static ScheduledExecutorService createScheduler(String name) {

		ValidationUtils.requireNonBlank(name, "name is required");

		return Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name(name).factory());
	}

	public static <T> void forEach(ExecutorService executor, Collection<T> items, Consumer<T> action) {

		ValidationUtils.requireNonNull(executor, "executor is required");
		ValidationUtils.requireNonNull(items, "items is required");
		ValidationUtils.requireNonNull(action, "action is required");

		var futures = items.stream()
			.map(item -> CompletableFuture.runAsync(() -> action.accept(item), executor))
			.toArray(CompletableFuture[]::new);

		CompletableFuture.allOf(futures).join();
	}

	public static <T> void forEach(ExecutorService executor, T[] items, Consumer<T> action) {

		ValidationUtils.requireNonNull(executor, "executor is required");
		ValidationUtils.requireNonNull(items, "items is required");
		ValidationUtils.requireNonNull(action, "action is required");

		forEach(executor, Arrays.asList(items), action);
	}

	public static void execute(ExecutorService executor, Runnable task) {

		ValidationUtils.requireNonNull(executor, "executor is required");
		ValidationUtils.requireNonNull(task, "task is required");

		executor.execute(task);
	}

	public static void shutdown(ExecutorService executorService, long timeoutSeconds, String context) {

		if (ValidationUtils.isNull(executorService)) {
			return;
		}

		try {

			executorService.shutdown();

			if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
				// in-flight event forwarding may be abandoned at this point; make it visible
				log.warn(context + " did not terminate gracefully within " + timeoutSeconds + " seconds, forcing shutdown");
				executorService.shutdownNow();
			}

		} catch (InterruptedException exception) {

			log.debug("Interrupted while waiting for " + context + " to terminate", exception);

			try {
				executorService.shutdownNow();
			} catch (Exception shutdownException) {
				log.debug("Failed to force shutdown " + context, shutdownException);
			}

			try {
				Thread.currentThread().interrupt();
			} catch (SecurityException securityException) {
				log.debug("Failed to interrupt current thread", securityException);
			}

		} catch (Exception exception) {
			log.debug("Failed to shutdown " + context, exception);
		}
	}

	public static void shutdown(ExecutorService executorService, String context) {

		if (ValidationUtils.isNull(executorService)) {
			return;
		}

		shutdown(executorService, 30, context);
	}
}
