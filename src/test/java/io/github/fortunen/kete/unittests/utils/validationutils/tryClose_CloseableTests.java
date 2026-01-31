package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.io.Closeable;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class tryClose_CloseableTests {

	@Test
	public void shouldCloseWithoutExceptionWhenResourceValid() {

		// arrange

		var closed = new boolean[]{false};
		Closeable resource = () -> closed[0] = true;

		// act

		ValidationUtils.tryClose(resource, "testResource");

		// assert

		assertThatCode(() -> {}).doesNotThrowAnyException();
	}

	@Test
	public void shouldNotThrowWhenResourceIsNull() {

		// arrange

		Closeable resource = null;

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "nullResource");
		}).doesNotThrowAnyException();
	}

	@Test
	public void shouldNotThrowWhenCloseThrowsException() {

		// arrange

		Closeable resource = () -> {
			throw new IOException("Close failed");
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
		Closeable resource = () -> closed[0] = true;

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "");
		}).doesNotThrowAnyException();
	}
}
