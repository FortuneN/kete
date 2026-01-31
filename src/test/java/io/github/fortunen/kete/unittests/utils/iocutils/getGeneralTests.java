package io.github.fortunen.kete.unittests.utils.iocutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.utils.IocUtils;
import org.junit.jupiter.api.Test;

public class getGeneralTests {

	@Test
	public void shouldReturnNullForUnknownKey() {

		// act

		var result = IocUtils.get("unknown-component", Destination.class);

		// assert

		assertThat(result)
			.as("Should return null for unknown component key")
			.isNull();
	}

	@Test
	public void shouldThrowWhenKeyIsNull() {

		// act

		var thrown = catchThrowable(() -> IocUtils.get(null, Destination.class));

		// assert

		assertThat(thrown)
			.as("Should throw exception when key is null")
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("key is required");
	}

	@Test
	public void shouldThrowWhenClazzIsNull() {

		// act

		var thrown = catchThrowable(() -> IocUtils.get("http", null));

		// assert

		assertThat(thrown)
			.as("Should throw exception when clazz is null")
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("clazz is required");
	}

	@Test
	public void shouldReturnNullForEmptyKey() {

		// act

		var result = IocUtils.get("", Destination.class);

		// assert

		assertThat(result)
			.as("Should return null for empty key")
			.isNull();
	}

	@Test
	public void shouldReturnNullForBlankKey() {

		// act

		var result = IocUtils.get("   ", Destination.class);

		// assert

		assertThat(result)
			.as("Should return null for blank key")
			.isNull();
	}
}
