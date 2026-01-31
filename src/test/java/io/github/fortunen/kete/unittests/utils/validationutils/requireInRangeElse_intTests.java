package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireInRangeElse_intTests {

	@Test
	public void shouldReturnValueWhenInRange() {

		// arrange

		var value = 5;
		var min = 1;
		var max = 10;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldReturnValueWhenEqualsMin() {

		// arrange

		var value = 1;
		var min = 1;
		var max = 10;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(1);
	}

	@Test
	public void shouldReturnValueWhenEqualsMax() {

		// arrange

		var value = 10;
		var min = 1;
		var max = 10;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldReturnDefaultWhenBelowMin() {

		// arrange

		var value = 0;
		var min = 1;
		var max = 10;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(100);
	}

	@Test
	public void shouldReturnDefaultWhenAboveMax() {

		// arrange

		var value = 11;
		var min = 1;
		var max = 10;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(100);
	}

	@Test
	public void shouldHandleNegativeRange() {

		// arrange

		var value = -5;
		var min = -10;
		var max = -1;
		var defaultValue = 0;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(-5);
	}

	@Test
	public void shouldHandleRangeCrossingZero() {

		// arrange

		var value = 0;
		var min = -10;
		var max = 10;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldHandleSingleValueRange() {

		// arrange

		var value = 5;
		var min = 5;
		var max = 5;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(5);
	}

	@Test
	public void shouldReturnDefaultForSingleValueRangeOutside() {

		// arrange

		var value = 6;
		var min = 5;
		var max = 5;
		var defaultValue = 100;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(100);
	}

	@Test
	public void shouldHandleNegativeDefault() {

		// arrange

		var value = 15;
		var min = 1;
		var max = 10;
		var defaultValue = -99;

		// act

		var result = ValidationUtils.requireInRangeElse(value, min, max, defaultValue);

		// assert

		assertThat(result).isEqualTo(-99);
	}

	@Test
	public void shouldHandlePortRange() {

		// arrange

		var validPort = 8080;
		var invalidPort = 70000;
		var minPort = 1;
		var maxPort = 65535;
		var defaultPort = 80;

		// act

		var result1 = ValidationUtils.requireInRangeElse(validPort, minPort, maxPort, defaultPort);
		var result2 = ValidationUtils.requireInRangeElse(invalidPort, minPort, maxPort, defaultPort);

		// assert

		assertThat(result1).isEqualTo(8080);
		assertThat(result2).isEqualTo(80);
	}
}
