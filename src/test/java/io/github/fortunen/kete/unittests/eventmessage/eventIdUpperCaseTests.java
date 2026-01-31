package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class eventIdUpperCaseTests {

	@Test
	void shouldReturnEventIdUpperCase() {

		// arrange

		var message = new EventMessage(null, "ABC-123-DEF", null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventIdUpperCase();

		// assert

		assertThat(result).isEqualTo("ABC-123-DEF");
	}

	@Test
	void shouldReturnNullWhenEventIdIsBlank() {

		// arrange

		var message = new EventMessage(null, "   ", null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventIdUpperCase();

		// assert

		assertThat(result).isNull();
	}
}
