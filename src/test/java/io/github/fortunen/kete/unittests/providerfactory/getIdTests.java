package io.github.fortunen.kete.unittests.providerfactory;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.ProviderFactory;
import org.junit.jupiter.api.Test;

class getIdTests {

	@Test
	void shouldReturnConstantsId() {

		// arrange

		var factory = new ProviderFactory();

		// act

		var result = factory.getId();

		// assert

		assertThat(result).isEqualTo(Constants.ID);
	}
}
