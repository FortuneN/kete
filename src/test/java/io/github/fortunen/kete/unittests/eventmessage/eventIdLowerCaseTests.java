package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class eventIdLowerCaseTests {

	@Test
	void shouldReturnEventIdLowerCase() {

		// arrange

		var message = new EventMessage(null, "ABC-123-DEF", null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventIdLowerCase();

		// assert

		assertThat(result).isEqualTo("abc-123-def");
	}

	@Test
	void shouldReturnNullWhenEventIdIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventIdLowerCase();

		// assert

		assertThat(result).isNull();
	}
}
