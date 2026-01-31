package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class eventTypeBytesTests {

	@Test
	void shouldReturnEventTypeAsBytes() {

		// arrange

		var message = new EventMessage(null, null, null, "LOGIN", null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeBytes();

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result)).isEqualTo("LOGIN");
	}

	@Test
	void shouldReturnNullWhenEventTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeBytes();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenEventTypeIsEmpty() {

		// arrange

		var message = new EventMessage(null, null, null, "", null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeBytes();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenEventTypeIsBlank() {

		// arrange

		var message = new EventMessage(null, null, null, "   ", null, null, Constants.EVENT, null, null);

		// act

		var result = message.eventTypeBytes();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnSameBytesForSameEventType() {

		// arrange

		var message1 = new EventMessage(null, null, null, "LOGIN", null, null, Constants.EVENT, null, null);
		var message2 = new EventMessage(null, null, null, "LOGIN", null, null, Constants.EVENT, null, null);

		// act

		var result1 = message1.eventTypeBytes();
		var result2 = message2.eventTypeBytes();

		// assert

		assertThat(result1).isSameAs(result2);
	}
}
