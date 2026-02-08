package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireValidUrlTests {

	@Test
	public void shouldReturnUrlWhenValidHttps() {

		// act

		var result = ValidationUtils.requireValidUrl("https://pubsub.googleapis.com", "url");

		// assert

		assertThat(result).isEqualTo("https://pubsub.googleapis.com");
	}

	@Test
	public void shouldReturnUrlWhenValidHttp() {

		// act

		var result = ValidationUtils.requireValidUrl("http://localhost:8085", "url");

		// assert

		assertThat(result).isEqualTo("http://localhost:8085");
	}

	@Test
	public void shouldReturnUrlWhenValidWithPath() {

		// act

		var result = ValidationUtils.requireValidUrl("https://example.com/api/v1", "url");

		// assert

		assertThat(result).isEqualTo("https://example.com/api/v1");
	}

	@Test
	public void shouldThrowWhenUrlIsNull() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidUrl(null, "url is required");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url is required");
	}

	@Test
	public void shouldThrowWhenUrlIsEmpty() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidUrl("", "url is required");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url is required");
	}

	@Test
	public void shouldThrowWhenUrlIsBlank() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidUrl("   ", "url is required");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url is required");
	}

	@Test
	public void shouldThrowWhenUrlHasNoScheme() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidUrl("pubsub.googleapis.com", "url must be absolute");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url must be absolute");
	}

	@Test
	public void shouldThrowWhenUrlIsRelativePath() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidUrl("/v1/projects/test", "url must be absolute");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url must be absolute");
	}

	@Test
	public void shouldThrowWhenUrlIsInvalidSyntax() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidUrl("ht tp://bad url", "url is invalid");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("url is invalid");
	}

	@Test
	public void shouldIncludeMessageInException() {

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.requireValidUrl("not-a-url", "custom error message");
		});

		// assert

		assertThat(thrown)
			.isNotNull()
			.hasMessage("custom error message");
	}
}
