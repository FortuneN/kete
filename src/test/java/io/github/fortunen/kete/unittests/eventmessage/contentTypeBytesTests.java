package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class contentTypeBytesTests {

	@Test
	void shouldReturnContentTypeAsBytes() {

		// arrange

		var message = new EventMessage(null, null, null, null, "application/json", null, Constants.EVENT, null, null);

		// act

		var result = message.contentTypeBytes();

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result)).isEqualTo("application/json");
	}

	@Test
	void shouldReturnNullWhenContentTypeIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.contentTypeBytes();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenContentTypeIsEmpty() {

		// arrange

		var message = new EventMessage(null, null, null, null, "", null, Constants.EVENT, null, null);

		// act

		var result = message.contentTypeBytes();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenContentTypeIsBlank() {

		// arrange

		var message = new EventMessage(null, null, null, null, "   ", null, Constants.EVENT, null, null);

		// act

		var result = message.contentTypeBytes();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnSameBytesForSameContentType() {

		// arrange

		var message1 = new EventMessage(null, null, null, null, "application/json", null, Constants.EVENT, null, null);
		var message2 = new EventMessage(null, null, null, null, "application/json", null, Constants.EVENT, null, null);

		// act

		var result1 = message1.contentTypeBytes();
		var result2 = message2.contentTypeBytes();

		// assert

		assertThat(result1).isSameAs(result2);
	}
}
