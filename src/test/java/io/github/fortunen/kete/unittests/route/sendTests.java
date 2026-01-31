package io.github.fortunen.kete.unittests.route;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Route;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class sendTests extends TestBase {

	@Test
	public void shouldThrowWhenMessageIsNull() {

		// arrange

		route = new Route();
		route.setDestinationPool(mock(GenericObjectPool.class));

		// act

		var thrown = catchThrowable(() -> route.send(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("message is required");
	}

	@Test
	public void shouldThrowWhenDestinationPoolIsNull() {

		// arrange

		route = new Route();
		var message = mock(EventMessage.class);

		// act

		var thrown = catchThrowable(() -> route.send(message));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("destinationPool is required");
	}

	@Test
	public void shouldBorrowDestinationFromPool() throws Exception {

		// arrange

		route = new Route();
		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var message = mock(EventMessage.class);

		// act

		route.send(message);

		// assert

		verify(pool).borrowObject();
	}

	@Test
	public void shouldSendMessageToDestination() throws Exception {

		// arrange

		route = new Route();
		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var message = mock(EventMessage.class);

		// act

		route.send(message);

		// assert

		verify(destination).send(message);
	}

	@Test
	public void shouldReturnDestinationToPoolAfterSend() throws Exception {

		// arrange

		route = new Route();
		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var message = mock(EventMessage.class);

		// act

		route.send(message);

		// assert

		verify(pool).returnObject(destination);
	}

	@Test
	public void shouldInvalidateDestinationOnSendFailure() throws Exception {

		// arrange

		route = new Route();
		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		doThrow(new RuntimeException("send failed")).when(destination).send(any());
		route.setDestinationPool(pool);

		var message = mock(EventMessage.class);

		// act

		var thrown = catchThrowable(() -> route.send(message));

		// assert

		assertThat(thrown).isInstanceOf(RuntimeException.class);
		verify(pool).invalidateObject(destination);
		verify(pool, never()).returnObject(any());
	}

	@Test
	public void shouldNotReturnDestinationOnSendFailure() throws Exception {

		// arrange

		route = new Route();
		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		doThrow(new RuntimeException("send failed")).when(destination).send(any());
		route.setDestinationPool(pool);

		var message = mock(EventMessage.class);

		// act

		catchThrowable(() -> route.send(message));

		// assert

		verify(pool, never()).returnObject(any());
	}

	@Test
	public void shouldUseRetryWhenConfigured() throws Exception {

		// arrange

		route = new Route();
		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var retryConfig = RetryConfig.custom().maxAttempts(3).build();
		var retry = Retry.of("test-retry", retryConfig);
		route.setRetry(retry);

		var message = mock(EventMessage.class);

		// act

		route.send(message);

		// assert

		verify(destination).send(message);
		verify(pool).returnObject(destination);
	}

	@Test
	public void shouldRetryOnFailureWhenRetryConfigured() throws Exception {

		// arrange

		route = new Route();
		var pool = mock(GenericObjectPool.class);
		var destination = mock(Destination.class);
		when(pool.borrowObject()).thenReturn(destination);
		route.setDestinationPool(pool);

		var retryConfig = RetryConfig.custom().maxAttempts(3).build();
		var retry = Retry.of("test-retry", retryConfig);
		route.setRetry(retry);

		var message = mock(EventMessage.class);

		doThrow(new RuntimeException("first failure"))
			.doThrow(new RuntimeException("second failure"))
			.doNothing()
			.when(destination).send(any());

		// act

		route.send(message);

		// assert

		verify(destination, org.mockito.Mockito.times(3)).send(message);
	}
}
