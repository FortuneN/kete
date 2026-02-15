package io.github.fortunen.kete.unittests.utils.protobufutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.utils.ProtobufUtils;

public class findMessageTests {

	@Test
	public void shouldFindEventMessage() {

		// act

		var result = ProtobufUtils.findMessage("Event");

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("Event");
	}

	@Test
	public void shouldFindAdminEventMessage() {

		// act

		var result = ProtobufUtils.findMessage("AdminEvent");

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("AdminEvent");
	}

	@Test
	public void shouldFindAuthDetailsMessage() {

		// act

		var result = ProtobufUtils.findMessage("AuthDetails");

		// assert

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("AuthDetails");
	}

	@Test
	public void shouldThrowWhenMessageNotFound() {

		// act

		var thrown = catchThrowable(() -> ProtobufUtils.findMessage("NonExistent"));

		// assert

		assertThat(thrown)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("NonExistent");
	}
}
