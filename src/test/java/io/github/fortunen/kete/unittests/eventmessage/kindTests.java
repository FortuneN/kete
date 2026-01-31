package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.EventMessage;
import org.junit.jupiter.api.Test;

class kindTests {

	@Test
	void shouldReturnEventWhenNotAdminEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.kind();

		// assert

		assertThat(result).isEqualTo(Constants.EVENT);
	}

	@Test
	void shouldReturnAdminEventWhenIsAdminEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.ADMIN_EVENT, null, null);

		// act

		var result = message.kind();

		// assert

		assertThat(result).isEqualTo(Constants.ADMIN_EVENT);
	}
}
