package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class resultPascalCaseTests {

	@Test
	void shouldReturnResultPascalCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, Constants.SUCCESS);

		// act

		var result = message.resultPascalCase();

		// assert

		assertThat(result).isEqualTo("Success");
	}

	@Test
	void shouldReturnNullWhenResultIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.resultPascalCase();

		// assert

		assertThat(result).isNull();
	}
}
