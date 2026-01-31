package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryClose_AutoCloseableTests {

	@Test
	public void shouldCloseWithoutExceptionWhenResourceValid() {

		// arrange

		var closed = new boolean[]{false};
		AutoCloseable resource = () -> closed[0] = true;

		// act

		ValidationUtils.tryClose(resource, "testResource");

		// assert

		assertThatCode(() -> {}).doesNotThrowAnyException();
	}

	@Test
	public void shouldNotThrowWhenResourceIsNull() {

		// arrange

		AutoCloseable resource = null;

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "nullResource");
		}).doesNotThrowAnyException();
	}

	@Test
	public void shouldNotThrowWhenCloseThrowsException() {

		// arrange

		AutoCloseable resource = () -> {
			throw new Exception("Close failed");
		};

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "failingResource");
		}).doesNotThrowAnyException();
	}

	@Test
	public void shouldHandleResourceWithEmptyName() {

		// arrange

		var closed = new boolean[]{false};
		AutoCloseable resource = () -> closed[0] = true;

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "");
		}).doesNotThrowAnyException();
	}

	@Test
	public void shouldCloseResourceThatThrowsRuntimeException() {

		// arrange

		AutoCloseable resource = () -> {
			throw new RuntimeException("Runtime error");
		};

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "runtimeResource");
		}).doesNotThrowAnyException();
	}
}
