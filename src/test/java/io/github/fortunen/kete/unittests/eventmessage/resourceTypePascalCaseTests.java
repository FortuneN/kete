package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class resourceTypePascalCaseTests {

	@Test
	void shouldReturnResourceTypePascalCase() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, "USER", Constants.EVENT, null, null);

		// act

		var result = message.resourceTypePascalCase();

		// assert

		assertThat(result).isEqualTo("User");
	}

	@Test
	void shouldReturnResourceTypePascalCaseMultiWord() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, "REALM_ROLE", Constants.EVENT, null, null);

		// act

		var result = message.resourceTypePascalCase();

		// assert

		assertThat(result).isEqualTo("RealmRole");
	}

	@Test
	void shouldReturnNullWhenResourceTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.resourceTypePascalCase();

		// assert

		assertThat(result).isNull();
	}
}
