package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class resultUpperCaseTests {

	@Test
	void shouldReturnResultUpperCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, Constants.SUCCESS);

		// act

		var result = message.resultUpperCase();

		// assert

		assertThat(result).isEqualTo("SUCCESS");
	}

	@Test
	void shouldReturnNullWhenResultIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.resultUpperCase();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenResultIsEmpty() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, "");

		// act

		var result = message.resultUpperCase();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenResultIsBlank() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, "   ");

		// act

		var result = message.resultUpperCase();

		// assert

		assertThat(result).isNull();
	}
}
