package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryClose_ObjectTests {

	@Test
	public void shouldCloseObjectWithCloseMethod() {

		// arrange

		var closed = new boolean[]{false};
		var resource = new Object() {
			@SuppressWarnings("unused")
			public void close() {
				closed[0] = true;
			}
		};

		// act

		ValidationUtils.tryClose(resource, "testResource");

		// assert

		assertThatCode(() -> {}).doesNotThrowAnyException();
	}

	@Test
	public void shouldNotThrowWhenResourceIsNull() {

		// arrange

		Object resource = null;

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "nullResource");
		}).doesNotThrowAnyException();
	}

	@Test
	public void shouldNotThrowWhenObjectHasNoCloseMethod() {

		// arrange

		var resource = new Object();

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "noCloseResource");
		}).doesNotThrowAnyException();
	}

	@Test
	public void shouldNotThrowWhenCloseMethodThrowsException() {

		// arrange

		var resource = new Object() {
			@SuppressWarnings("unused")
			public void close() {
				throw new RuntimeException("Close failed");
			}
		};

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "failingResource");
		}).doesNotThrowAnyException();
	}

	@Test
	public void shouldHandleStringAsResource() {

		// arrange

		var resource = "test string";

		// act & assert

		assertThatCode(() -> {
			ValidationUtils.tryClose(resource, "stringResource");
		}).doesNotThrowAnyException();
	}
}
