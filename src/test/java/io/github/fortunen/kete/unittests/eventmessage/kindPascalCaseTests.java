package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class kindPascalCaseTests {

	@Test
	void shouldReturnKindPascalCaseForEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.kindPascalCase();

		// assert

		assertThat(result).isEqualTo("Event");
	}

	@Test
	void shouldReturnKindPascalCaseForAdminEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.ADMIN_EVENT, null, null);

		// act

		var result = message.kindPascalCase();

		// assert

		assertThat(result).isEqualTo("AdminEvent");
	}

	@Test
	void shouldReturnNullWhenKindIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, null, null, null);

		// act

		var result = message.kindPascalCase();

		// assert

		assertThat(result).isNull();
	}
}
