package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class realmUpperCaseTests {

	@Test
	void shouldReturnRealmUpperCase() {

		// arrange

		var message = new EventMessage("MASTER", null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.realmUpperCase();

		// assert

		assertThat(result).isEqualTo("MASTER");
	}

	@Test
	void shouldReturnNullWhenRealmIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.realmUpperCase();

		// assert

		assertThat(result).isNull();
	}
}
