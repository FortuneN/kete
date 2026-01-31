package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class kindLowerCaseTests {

	@Test
	void shouldReturnKindLowerCaseForEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.kindLowerCase();

		// assert

		assertThat(result).isEqualTo("event");
	}

	@Test
	void shouldReturnKindLowerCaseForAdminEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.ADMIN_EVENT, null, null);

		// act

		var result = message.kindLowerCase();

		// assert

		assertThat(result).isEqualTo("admin_event");
	}
}
