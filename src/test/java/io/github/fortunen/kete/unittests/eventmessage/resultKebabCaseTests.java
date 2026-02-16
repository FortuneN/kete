package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class resultKebabCaseTests {

	@Test
	void shouldReturnResultKebabCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, Constants.SUCCESS);

		// act

		var result = message.resultKebabCase();

		// assert

		assertThat(result).isEqualTo("success");
	}

	@Test
	void shouldReturnNullWhenResultIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.resultKebabCase();

		// assert

		assertThat(result).isNull();
	}
}
