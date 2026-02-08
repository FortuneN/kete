package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class kindBytesTests {

	@Test
	void shouldReturnKindAsBytesForEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.kindBytes();

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result)).isEqualTo("EVENT");
	}

	@Test
	void shouldReturnKindAsBytesForAdminEvent() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.ADMIN_EVENT, null, null);

		// act

		var result = message.kindBytes();

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result)).isEqualTo("ADMIN_EVENT");
	}

	@Test
	void shouldReturnSameBytesForSameKind() {

		// arrange

		var message1 = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);
		var message2 = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result1 = message1.kindBytes();
		var result2 = message2.kindBytes();

		// assert

		assertThat(result1).isSameAs(result2);
	}

	@Test
	void shouldReturnNullForNullKind() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, null, null, null);

		// act

		var result = message.kindBytes();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullForBlankKind() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, "   ", null, null);

		// act

		var result = message.kindBytes();

		// assert

		assertThat(result).isNull();
	}
}
