package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class kindUpperCaseTests {

	@Test
	void shouldReturnKindUpperCaseForEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.kindUpperCase();

		// assert

		assertThat(result).isEqualTo("EVENT");
	}

	@Test
	void shouldReturnKindUpperCaseForAdminEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.ADMIN_EVENT, null, null);

		// act

		var result = message.kindUpperCase();

		// assert

		assertThat(result).isEqualTo("ADMIN_EVENT");
	}
}
