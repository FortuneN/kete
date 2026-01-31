package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class resourceTypeUpperCaseTests {

	@Test
	void shouldReturnResourceTypeUpperCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, "USER", Constants.EVENT, null, null);

		// act

		var result = message.resourceTypeUpperCase();

		// assert

		assertThat(result).isEqualTo("USER");
	}

	@Test
	void shouldReturnNullWhenResourceTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.resourceTypeUpperCase();

		// assert

		assertThat(result).isNull();
	}
}
