package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class clearCacheTests {

	@Test
	void shouldClearCacheWithoutException() {

		// arrange

		var message = new EventMessage("TEST", null, null, "LOGIN", null, null, Constants.EVENT, null, Constants.SUCCESS);

		// populate caches
		message.realmLowerCase();
		message.realmUpperCase();
		message.eventTypeLowerCase();
		message.eventTypeUpperCase();
		message.kindKebabCase();
		message.kindPascalCase();
		message.kindCamelCase();

		// act & assert

		assertThatCode(() -> EventMessage.clearCache()).doesNotThrowAnyException();
	}

	@Test
	void shouldClearCacheWhenEmpty() {

		// act & assert

		assertThatCode(() -> EventMessage.clearCache()).doesNotThrowAnyException();
	}
}
