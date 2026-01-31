package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class operationTypeUpperCaseTests {

	@Test
	void shouldReturnOperationTypeUpperCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, "delete", null);

		// act

		var result = message.operationTypeUpperCase();

		// assert

		assertThat(result).isEqualTo("DELETE");
	}

	@Test
	void shouldReturnNullWhenOperationTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.operationTypeUpperCase();

		// assert

		assertThat(result).isNull();
	}
}
