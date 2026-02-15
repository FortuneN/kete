package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class kindKebabCaseTests {

	@Test
	void shouldReturnKindKebabCaseForEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.kindKebabCase();

		// assert

		assertThat(result).isEqualTo("event");
	}

	@Test
	void shouldReturnKindKebabCaseForAdminEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.ADMIN_EVENT, null, null);

		// act

		var result = message.kindKebabCase();

		// assert

		assertThat(result).isEqualTo("admin-event");
	}

	@Test
	void shouldReturnNullWhenKindIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, null, null, null);

		// act

		var result = message.kindKebabCase();

		// assert

		assertThat(result).isNull();
	}
}
