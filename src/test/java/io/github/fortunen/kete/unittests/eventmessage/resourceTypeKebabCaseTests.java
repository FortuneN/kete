package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class resourceTypeKebabCaseTests {

	@Test
	void shouldReturnResourceTypeKebabCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, "USER", Constants.EVENT, null, null);

		// act

		var result = message.resourceTypeKebabCase();

		// assert

		assertThat(result).isEqualTo("user");
	}

	@Test
	void shouldReturnResourceTypeKebabCaseMultiWord() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, "REALM_ROLE", Constants.EVENT, null, null);

		// act

		var result = message.resourceTypeKebabCase();

		// assert

		assertThat(result).isEqualTo("realm-role");
	}

	@Test
	void shouldReturnNullWhenResourceTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.resourceTypeKebabCase();

		// assert

		assertThat(result).isNull();
	}
}
