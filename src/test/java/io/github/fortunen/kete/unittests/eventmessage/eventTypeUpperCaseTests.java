package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class eventTypeUpperCaseTests {

	@Test
	void shouldReturnEventTypeUpperCase() {

		// arrange

		var message = new EventMessage(null, null, null, "LOGIN", null, null, Constants.EVENT, null, Constants.SUCCESS);

		// act

		var result = message.eventTypeUpperCase();

		// assert

		assertThat(result).isEqualTo("LOGIN");
	}

	@Test
	void shouldReturnNullWhenEventTypeIsBlank() {

		// arrange

		var message = new EventMessage(null, null, null, "", null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeUpperCase();

		// assert

		assertThat(result).isNull();
	}
}
