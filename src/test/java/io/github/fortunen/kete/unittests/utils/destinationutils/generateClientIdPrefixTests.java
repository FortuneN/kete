package io.github.fortunen.kete.unittests.utils.destinationutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.utils.DestinationUtils;

public class generateClientIdPrefixTests {

	@Test
	public void shouldReturnNonNullValue() {

		// act

		var result = DestinationUtils.generateClientIdPrefix();

		// assert

		assertThat(result).isNotNull();
	}

	@Test
	public void shouldStartWithConstantsId() {

		// act

		var result = DestinationUtils.generateClientIdPrefix();

		// assert

		assertThat(result).startsWith(Constants.ID + "-");
	}

	@Test
	public void shouldContainUuidAfterPrefix() {

		// act

		var result = DestinationUtils.generateClientIdPrefix();

		// assert

		var uuid = result.substring(Constants.ID.length() + 1);
		assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
	}

	@Test
	public void shouldReturnLowercaseUuid() {

		// act

		var result = DestinationUtils.generateClientIdPrefix();

		// assert

		var uuid = result.substring(Constants.ID.length() + 1);
		assertThat(uuid).isEqualTo(uuid.toLowerCase());
	}

	@Test
	public void shouldReturnDifferentValuesOnEachCall() {

		// act

		var result1 = DestinationUtils.generateClientIdPrefix();
		var result2 = DestinationUtils.generateClientIdPrefix();

		// assert

		assertThat(result1).isNotEqualTo(result2);
	}
}
