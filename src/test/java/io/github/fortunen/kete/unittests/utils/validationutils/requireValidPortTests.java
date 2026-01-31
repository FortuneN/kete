package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireValidPortTests {

	@Test
	public void shouldReturnPortWhenValid() {

		// act

		var result = ValidationUtils.requireValidPort(8080, "port");

		// assert

		assertThat(result).isEqualTo(8080);
	}

	@Test
	public void shouldReturnPortWhenMinValid() {

		// act

		var result = ValidationUtils.requireValidPort(1, "port");

		// assert

		assertThat(result).isEqualTo(1);
	}

	@Test
	public void shouldReturnPortWhenMaxValid() {

		// act

		var result = ValidationUtils.requireValidPort(65535, "port");

		// assert

		assertThat(result).isEqualTo(65535);
	}

	@Test
	public void shouldThrowWhenPortIsZero() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidPort(0, "port");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	@Test
	public void shouldThrowWhenPortIsNegative() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidPort(-1, "port");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	@Test
	public void shouldThrowWhenPortExceedsMax() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidPort(65536, "port");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("port must be between 1 and 65535");
	}

	@Test
	public void shouldIncludeFieldNameInMessage() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidPort(0, "serverPort");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("serverPort must be between 1 and 65535");
	}

	@Test
	public void shouldReturnCommonHttpPort() {

		// act

		var result = ValidationUtils.requireValidPort(80, "httpPort");

		// assert

		assertThat(result).isEqualTo(80);
	}

	@Test
	public void shouldReturnCommonHttpsPort() {

		// act

		var result = ValidationUtils.requireValidPort(443, "httpsPort");

		// assert

		assertThat(result).isEqualTo(443);
	}
}
