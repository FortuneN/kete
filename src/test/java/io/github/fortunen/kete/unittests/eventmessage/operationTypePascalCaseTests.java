package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class operationTypePascalCaseTests {

	@Test
	void shouldReturnOperationTypePascalCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, "CREATE", null);

		// act

		var result = message.operationTypePascalCase();

		// assert

		assertThat(result).isEqualTo("Create");
	}

	@Test
	void shouldReturnNullWhenOperationTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.operationTypePascalCase();

		// assert

		assertThat(result).isNull();
	}
}
