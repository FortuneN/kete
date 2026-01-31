package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class tryParseByteTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveByte() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("123")).hasValue((byte) 123);
	}

	@Test
	public void shouldParseNegativeByte() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("-56")).hasValue((byte) -56);
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("0")).hasValue((byte) 0);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("  42  ")).hasValue((byte) 42);
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("abc")).isEmpty();
	}

	@Test
	public void shouldParseMaxByte() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("127")).hasValue(Byte.MAX_VALUE);
	}

	@Test
	public void shouldParseMinByte() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("-128")).hasValue(Byte.MIN_VALUE);
	}

	@Test
	public void shouldReturnEmptyForOverflow() {

		// act & assert

		assertThat(ValidationUtils.tryParseByte("128")).isEmpty();
	}
}
