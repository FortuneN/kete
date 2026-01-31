package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class checkFromToIndexTests {

	@Test
	public void shouldPassWhenRangeValid() {

		// arrange

		var fromIndex = 2;
		var toIndex = 5;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);

		// assert

		assertThat(result).isEqualTo(2);
	}

	@Test
	public void shouldPassWhenRangeStartsAtZero() {

		// arrange

		var fromIndex = 0;
		var toIndex = 5;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldPassWhenRangeEndsAtLength() {

		// arrange

		var fromIndex = 5;
		var toIndex = 10;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldPassWhenRangeEmpty() {

		// arrange

		var fromIndex = 5;
		var toIndex = 5;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldPassWhenRangeFullLength() {

		// arrange

		var fromIndex = 0;
		var toIndex = 10;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldThrowWhenFromIndexNegative() {

		// arrange

		var fromIndex = -1;
		var toIndex = 5;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [-1, 5) out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenToIndexGreaterThanLength() {

		// arrange

		var fromIndex = 5;
		var toIndex = 15;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [5, 15) out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenFromIndexGreaterThanToIndex() {

		// arrange

		var fromIndex = 7;
		var toIndex = 5;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [7, 5) out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenBothIndicesNegative() {

		// arrange

		var fromIndex = -5;
		var toIndex = -3;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [-5, -3) out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenLengthZero() {

		// arrange

		var fromIndex = 0;
		var toIndex = 1;
		var length = 0;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [0, 1) out of bounds for length 0");
	}

	@Test
	public void shouldPassWhenLengthZeroEmptyRange() {

		// arrange

		var fromIndex = 0;
		var toIndex = 0;
		var length = 0;

		// act

		var result = ValidationUtils.checkFromToIndex(fromIndex, toIndex, length);

		// assert

		assertThat(result).isEqualTo(0);
	}
}
