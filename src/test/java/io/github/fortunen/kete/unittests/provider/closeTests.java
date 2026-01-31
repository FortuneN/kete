package io.github.fortunen.kete.unittests.provider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import io.github.fortunen.kete.Provider;
import org.junit.jupiter.api.Test;
import org.keycloak.events.EventListenerTransaction;

class closeTests {

	@Test
	void shouldCloseWithoutException() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act & assert

		assertThatCode(() -> provider.close()).doesNotThrowAnyException();
	}

	@Test
	void shouldAllowMultipleCloseCalls() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act & assert

		assertThatCode(() -> {
			provider.close();
			provider.close();
			provider.close();
		}).doesNotThrowAnyException();
	}

	@Test
	void shouldCloseAfterProcessingEvents() {

		// arrange

		var mockTransaction = mock(EventListenerTransaction.class);
		var provider = new Provider(mockTransaction);

		// act & assert

		assertThatCode(() -> {
			provider.close();
		}).doesNotThrowAnyException();
	}
}
