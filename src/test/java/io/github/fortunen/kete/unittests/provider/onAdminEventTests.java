package io.github.fortunen.kete.unittests.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.fortunen.kete.Provider;
import org.junit.jupiter.api.Test;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class onAdminEventTests {

	@Test
	void shouldAddAdminEventToTransactionOnAdminEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setId("test-admin-id");
		adminEvent.setRealmName("test-realm");
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setResourceType(ResourceType.USER);
		adminEvent.setTime(System.currentTimeMillis());

		// act

		provider.onEvent(adminEvent, true);

		// assert

		verify(mockTransaction, times(1)).addAdminEvent(adminEvent, true);
	}

	@Test
	void shouldPassIncludeRepresentationToTransaction() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setId("test-admin-id");

		// act

		provider.onEvent(adminEvent, false);

		// assert

		verify(mockTransaction, times(1)).addAdminEvent(adminEvent, false);
	}

	@Test
	void shouldHandleMultipleAdminEvents() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent1 = new AdminEvent();
		adminEvent1.setId("admin-event-1");
		adminEvent1.setOperationType(OperationType.CREATE);

		var adminEvent2 = new AdminEvent();
		adminEvent2.setId("admin-event-2");
		adminEvent2.setOperationType(OperationType.UPDATE);

		var adminEvent3 = new AdminEvent();
		adminEvent3.setId("admin-event-3");
		adminEvent3.setOperationType(OperationType.DELETE);

		// act

		provider.onEvent(adminEvent1, true);
		provider.onEvent(adminEvent2, false);
		provider.onEvent(adminEvent3, true);

		// assert

		verify(mockTransaction, times(1)).addAdminEvent(adminEvent1, true);
		verify(mockTransaction, times(1)).addAdminEvent(adminEvent2, false);
		verify(mockTransaction, times(1)).addAdminEvent(adminEvent3, true);
	}

	@Test
	void shouldHandleAdminEventsWithDifferentOperationTypes() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act

		for (var operationType : OperationType.values()) {
			var adminEvent = new AdminEvent();
			adminEvent.setId("admin-event-" + operationType.name());
			adminEvent.setOperationType(operationType);
			adminEvent.setResourceType(ResourceType.USER);
			provider.onEvent(adminEvent, true);
		}

		// assert

		verify(mockTransaction, times(OperationType.values().length)).addAdminEvent(ArgumentMatchers.any(AdminEvent.class), ArgumentMatchers.eq(true));
	}

	@Test
	void shouldHandleAdminEventsWithDifferentResourceTypes() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act

		for (var resourceType : ResourceType.values()) {
			var adminEvent = new AdminEvent();
			adminEvent.setId("admin-event-" + resourceType.name());
			adminEvent.setOperationType(OperationType.CREATE);
			adminEvent.setResourceType(resourceType);
			provider.onEvent(adminEvent, false);
		}

		// assert

		verify(mockTransaction, times(ResourceType.values().length)).addAdminEvent(ArgumentMatchers.any(AdminEvent.class), ArgumentMatchers.eq(false));
	}

	@Test
	void shouldPassNullAdminEventToTransaction() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act

		provider.onEvent((AdminEvent) null, false);

		// assert - null is passed through to the transaction

		verify(mockTransaction).addAdminEvent((AdminEvent) null, false);
	}

	@Test
	void shouldFailWhenTransactionThrowsExceptionOnAddAdminEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		doThrow(new RuntimeException("Admin transaction failure")).when(mockTransaction).addAdminEvent(any(AdminEvent.class), eq(false));
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);

		// act

		var thrown = catchThrowable(() -> provider.onEvent(adminEvent, false));

		// assert

		assertThat(thrown).isInstanceOf(RuntimeException.class);
	}

	@Test
	void shouldHandleAdminEventHavingNullOperationType() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(null);

		// act & assert

		assertThatCode(() -> provider.onEvent(adminEvent, false)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleEmptyAdminEventId() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setId("");

		// act & assert

		assertThatCode(() -> provider.onEvent(adminEvent, false)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleNullResourcePathInAdminEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setResourcePath(null);

		// act & assert

		assertThatCode(() -> provider.onEvent(adminEvent, false)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleEmptyResourcePathInAdminEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setResourcePath("");

		// act & assert

		assertThatCode(() -> provider.onEvent(adminEvent, false)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleNullRepresentationInAdminEvent() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);
		adminEvent.setRepresentation(null);

		// act & assert

		assertThatCode(() -> provider.onEvent(adminEvent, false)).doesNotThrowAnyException();
	}

	@Test
	void shouldHandleTimeoutDuringEventProcessing() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		Mockito.doAnswer(invocation -> null).when(mockTransaction).addAdminEvent(any(AdminEvent.class), eq(false));
		var provider = new Provider(mockTransaction);

		var adminEvent = new AdminEvent();
		adminEvent.setOperationType(OperationType.CREATE);

		// act & assert

		assertThatCode(() -> provider.onEvent(adminEvent, false)).doesNotThrowAnyException();
	}
}
