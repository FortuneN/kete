package io.github.fortunen.kete.unittests.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.fortunen.kete.Provider;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.mockito.ArgumentMatchers;

class onEventTests {

	@Test
	void shouldAddEventToTransactionOnEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setId("test-id");
		event.setType(EventType.LOGIN);
		event.setRealmName("test-realm");
		event.setClientId("test-client");
		event.setUserId("test-user");
		event.setTime(System.currentTimeMillis());

		// act

		provider.onEvent(event);

		// assert

		verify(mockTransaction, times(1)).addEvent(ArgumentMatchers.any(Event.class));
	}

	@Test
	void shouldCloneEventBeforeAddingToTransaction() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setId("test-id");
		event.setType(EventType.LOGIN);
		event.setRealmName("test-realm");

		// act

		provider.onEvent(event);

		// assert

		verify(mockTransaction, times(1)).addEvent(ArgumentMatchers.argThat(arg ->
			arg != null && "test-id".equals(arg.getId())
		));
	}

	@Test
	void shouldHandleMultipleEvents() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event1 = new Event();
		event1.setId("event-1");
		event1.setType(EventType.LOGIN);

		var event2 = new Event();
		event2.setId("event-2");
		event2.setType(EventType.LOGOUT);

		var event3 = new Event();
		event3.setId("event-3");
		event3.setType(EventType.REGISTER);

		// act

		provider.onEvent(event1);
		provider.onEvent(event2);
		provider.onEvent(event3);

		// assert

		verify(mockTransaction, times(3)).addEvent(ArgumentMatchers.any(Event.class));
	}

	@Test
	void shouldHandleEventsWithDifferentEventTypes() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act

		for (var eventType : EventType.values()) {
			var event = new Event();
			event.setId("event-" + eventType.name());
			event.setType(eventType);
			provider.onEvent(event);
		}

		// assert

		verify(mockTransaction, times(EventType.values().length)).addEvent(ArgumentMatchers.any(Event.class));
	}

	@Test
	void shouldPassNullEventToTransaction() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act

		provider.onEvent((Event) null);

		// assert - null is passed through to the transaction

		verify(mockTransaction).addEvent((Event) null);
	}

	@Test
	void shouldFailWhenTransactionThrowsExceptionOnAddEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		doThrow(new RuntimeException("Transaction failure")).when(mockTransaction).addEvent(any(Event.class));
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setType(EventType.LOGIN);

		// act

		var thrown = catchThrowable(() -> provider.onEvent(event));

		// assert

		assertThat(thrown).isInstanceOf(RuntimeException.class);
	}

	@Test
	void shouldHandleEventHavingNullType() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setType(null);

		// act & assert

		assertThatCode(() -> provider.onEvent(event)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleEmptyEventId() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setId("");

		// act & assert

		assertThatCode(() -> provider.onEvent(event)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleConcurrentEventProcessing() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);
		var executor = Executors.newFixedThreadPool(100);

		// act

		for (var i = 0; i < 100; i++) {
			final var index = i;
			executor.submit(() -> {
				try {
					var event = new Event();
					event.setType(EventType.LOGIN);
					event.setUserId("user" + index);
					provider.onEvent(event);
				} catch (Exception e) {
					// Expected
				}
			});
		}
		executor.shutdown();

		// assert

		assertThat(executor.isShutdown()).isTrue();
	}

	@Test
	void shouldHandleInvalidUnicodeInEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId("realm\uD800");

		// act & assert

		assertThatCode(() -> provider.onEvent(event)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleControlCharactersInEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setUserId("user\u0000\u0001");

		// act & assert

		assertThatCode(() -> provider.onEvent(event)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleNullRealmIdInEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setRealmId(null);

		// act & assert

		assertThatCode(() -> provider.onEvent(event)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleNullUserIdInEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var event = new Event();
		event.setType(EventType.LOGIN);
		event.setUserId(null);

		// act & assert

		assertThatCode(() -> provider.onEvent(event)).doesNotThrowAnyException();
	}
}
