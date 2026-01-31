package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class checkIndexTests {

	@Test
	public void shouldPassWhenIndexValidAtStart() {

		// arrange

		var index = 0;
		var length = 10;

		// act

		var result = ValidationUtils.checkIndex(index, length);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldPassWhenIndexValidAtEnd() {

		// arrange

		var index = 9;
		var length = 10;

		// act

		var result = ValidationUtils.checkIndex(index, length);

		// assert

		assertThat(result).isEqualTo(9);
	}

	@Test
	public void shouldPassWhenIndexValidInMiddle() {

		// arrange

		var index = 5;
		var length = 10;

		// act

		var result = ValidationUtils.checkIndex(index, length);

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldThrowWhenIndexNegative() {

		// arrange

		var index = -1;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkIndex(index, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Index -1 out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenIndexEqualsLength() {

		// arrange

		var index = 10;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkIndex(index, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Index 10 out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenIndexGreaterThanLength() {

		// arrange

		var index = 15;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkIndex(index, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Index 15 out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenLengthZero() {

		// arrange

		var index = 0;
		var length = 0;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkIndex(index, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Index 0 out of bounds for length 0");
	}

	@Test
	public void shouldPassForSingleElementArray() {

		// arrange

		var index = 0;
		var length = 1;

		// act

		var result = ValidationUtils.checkIndex(index, length);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldThrowForSingleElementArrayInvalidIndex() {

		// arrange

		var index = 1;
		var length = 1;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkIndex(index, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Index 1 out of bounds for length 1");
	}

	@Test
	public void shouldThrowWhenIndexVeryNegative() {

		// arrange

		var index = Integer.MIN_VALUE;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkIndex(index, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Index -2147483648 out of bounds for length 10");
	}
}
