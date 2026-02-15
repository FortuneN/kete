package io.github.fortunen.kete.unittests.eventmessage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.Constants;
import org.junit.jupiter.api.Test;

class realmCamelCaseTests {

	@Test
	void shouldReturnRealmCamelCase() {

		// arrange

		var message = new EventMessage("MASTER", null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.realmCamelCase();

		// assert

		assertThat(result).isEqualTo("master");
	}

	@Test
	void shouldReturnRealmCamelCaseMultiWord() {

		// arrange

		var message = new EventMessage("MY_REALM", null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.realmCamelCase();

		// assert

		assertThat(result).isEqualTo("myRealm");
	}

	@Test
	void shouldReturnNullWhenRealmIsNull() {

		// arrange

		var message = new EventMessage(null, null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.realmCamelCase();

		// assert

		assertThat(result).isNull();
	}

	@Test
	void shouldReturnNullWhenRealmIsBlank() {

		// arrange

		var message = new EventMessage("   ", null, null, null, null, null, Constants.EVENT, null, null);

		// act

		var result = message.realmCamelCase();

		// assert

		assertThat(result).isNull();
	}
}
