package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class tryParseBigIntegerTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("   ")).isEmpty();
	}

	@Test
	public void shouldParsePositiveBigInteger() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("12345678901234567890"))
			.hasValue(new BigInteger("12345678901234567890"));
	}

	@Test
	public void shouldParseNegativeBigInteger() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("-12345678901234567890"))
			.hasValue(new BigInteger("-12345678901234567890"));
	}

	@Test
	public void shouldParseZero() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("0")).hasValue(BigInteger.ZERO);
	}

	@Test
	public void shouldTrimWhitespace() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("  42  ")).hasValue(BigInteger.valueOf(42));
	}

	@Test
	public void shouldReturnEmptyForInvalidString() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("abc")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForFloat() {

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger("3.14")).isEmpty();
	}

	@Test
	public void shouldParseVeryLargeNumber() {

		// arrange

		var veryLarge = "999999999999999999999999999999999999999999999999";

		// act & assert

		assertThat(ValidationUtils.tryParseBigInteger(veryLarge)).hasValue(new BigInteger(veryLarge));
	}
}
