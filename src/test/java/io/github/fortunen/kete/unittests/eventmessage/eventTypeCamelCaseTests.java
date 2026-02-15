package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class eventTypeCamelCaseTests {

	@Test
	void shouldReturnEventTypeCamelCase() {

		// arrange

		var message = new EventMessage(null, null, null, "LOGIN", null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeCamelCase();

		// assert

		assertThat(result).isEqualTo("login");
	}

	@Test
	void shouldReturnEventTypeCamelCaseMultiWord() {

		// arrange

		var message = new EventMessage(null, null, null, "LOGIN_ERROR", null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeCamelCase();

		// assert

		assertThat(result).isEqualTo("loginError");
	}

	@Test
	void shouldReturnNullWhenEventTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeCamelCase();

		// assert

		assertThat(result).isNull();
	}
}
