package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class resourceTypeLowerCaseTests {

	@Test
	void shouldReturnResourceTypeLowerCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, "USER", Constants.EVENT, null, null);

		// act

		var result = message.resourceTypeLowerCase();

		// assert

		assertThat(result).isEqualTo("user");
	}

	@Test
	void shouldReturnNullWhenResourceTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.resourceTypeLowerCase();

		// assert

		assertThat(result).isNull();
	}
}
