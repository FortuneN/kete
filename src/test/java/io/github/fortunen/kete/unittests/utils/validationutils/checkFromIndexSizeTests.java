package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class checkFromIndexSizeTests {

	@Test
	public void shouldPassWhenRangeValid() {

		// arrange

		var fromIndex = 2;
		var size = 3;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromIndexSize(fromIndex, size, length);

		// assert

		assertThat(result).isEqualTo(2);
	}

	@Test
	public void shouldPassWhenRangeStartsAtZero() {

		// arrange

		var fromIndex = 0;
		var size = 5;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromIndexSize(fromIndex, size, length);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldPassWhenRangeEndsAtLength() {

		// arrange

		var fromIndex = 5;
		var size = 5;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromIndexSize(fromIndex, size, length);

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldPassWhenSizeZero() {

		// arrange

		var fromIndex = 5;
		var size = 0;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromIndexSize(fromIndex, size, length);

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldPassWhenRangeFullLength() {

		// arrange

		var fromIndex = 0;
		var size = 10;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromIndexSize(fromIndex, size, length);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldThrowWhenFromIndexNegative() {

		// arrange

		var fromIndex = -1;
		var size = 5;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromIndexSize(fromIndex, size, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [-1, 4) out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenSizeNegative() {

		// arrange

		var fromIndex = 5;
		var size = -3;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromIndexSize(fromIndex, size, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [5, 2) out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenRangeExceedsLength() {

		// arrange

		var fromIndex = 7;
		var size = 5;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromIndexSize(fromIndex, size, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [7, 12) out of bounds for length 10");
	}

	@Test
	public void shouldThrowWhenFromIndexEqualsLength() {

		// arrange

		var fromIndex = 10;
		var size = 1;
		var length = 10;

		// act

		var thrown = catchThrowable(() -> {
			ValidationUtils.checkFromIndexSize(fromIndex, size, length);
		});

		// assert

		assertThat(thrown.getMessage()).isEqualTo("Range [10, 11) out of bounds for length 10");
	}

	@Test
	public void shouldPassWhenFromIndexEqualsLengthSizeZero() {

		// arrange

		var fromIndex = 10;
		var size = 0;
		var length = 10;

		// act

		var result = ValidationUtils.checkFromIndexSize(fromIndex, size, length);

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldPassWhenLengthZeroEmptyRange() {

		// arrange

		var fromIndex = 0;
		var size = 0;
		var length = 0;

		// act

		var result = ValidationUtils.checkFromIndexSize(fromIndex, size, length);

		// assert

		assertThat(result).isEqualTo(0);
	}
}
