package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class operationTypeKebabCaseTests {

	@Test
	void shouldReturnOperationTypeKebabCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, "CREATE", null);

		// act

		var result = message.operationTypeKebabCase();

		// assert

		assertThat(result).isEqualTo("create");
	}

	@Test
	void shouldReturnNullWhenOperationTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.operationTypeKebabCase();

		// assert

		assertThat(result).isNull();
	}
}
