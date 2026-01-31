package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonNullElseGetTests {

	@Test
	public void shouldReturnValueWhenNonNull() {

		// arrange

		var value = "test";
		var invoked = new boolean[] { false };

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> {
			invoked[0] = true;
			return "default";
		});

		// assert

		assertThat(result).isEqualTo("test");
		assertThat(invoked[0]).isEqualTo(false);
	}

	@Test
	public void shouldReturnSupplierValueWhenNull() {

		// arrange

		String value = null;
		var invoked = new boolean[] { false };

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> {
			invoked[0] = true;
			return "default";
		});

		// assert

		assertThat(result).isEqualTo("default");
		assertThat(invoked[0]).isEqualTo(true);
	}

	@Test
	public void shouldReturnNullWhenSupplierReturnsNull() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> null);

		// assert

		assertThat(result).isNull();
	}

	@Test
	public void shouldHandleIntegerValues() {

		// arrange

		Integer value = null;

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> 42);

		// assert

		assertThat(result).isEqualTo(42);
	}

	@Test
	public void shouldHandleEmptyString() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> "default");

		// assert

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldHandleZeroValue() {

		// arrange

		var value = 0;

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> 42);

		// assert

		assertThat(result).isEqualTo(0);
	}

	@Test
	public void shouldInvokeSupplierOnlyWhenNeeded() {

		// arrange

		var value = "test";
		var invocationCount = new int[] { 0 };

		// act

		var result1 = ValidationUtils.requireNonNullElseGet(value, () -> {
			invocationCount[0]++;
			return "default";
		});

		String nullValue = null;
		var result2 = ValidationUtils.requireNonNullElseGet(nullValue, () -> {
			invocationCount[0]++;
			return "default";
		});

		// assert

		assertThat(result1).isEqualTo("test");
		assertThat(result2).isEqualTo("default");
		assertThat(invocationCount[0]).isEqualTo(1);
	}

	@Test
	public void shouldHandleComplexSupplier() {

		// arrange

		String value = null;

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> {
			var sb = new StringBuilder();
			sb.append("computed");
			sb.append("-");
			sb.append("value");
			return sb.toString();
		});

		// assert

		assertThat(result).isEqualTo("computed-value");
	}

	@Test
	public void shouldHandleArraysFromSupplier() {

		// arrange

		String[] value = null;

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, () -> new String[] { "a", "b" });

		// assert

		assertThat(result.length).isEqualTo(2);
		assertThat(result[0]).isEqualTo("a");
	}

	@Test
	public void shouldHandleLazyComputation() {

		// arrange

		String value = null;
		var expensive = new Object() {
			int computationCount = 0;

			String compute() {
				computationCount++;
				return "expensive-result-" + computationCount;
			}
		};

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, expensive::compute);

		// assert

		assertThat(result).isEqualTo("expensive-result-1");
		assertThat(expensive.computationCount).isEqualTo(1);
	}

	@Test
	public void shouldNotComputeWhenValuePresent() {

		// arrange

		var value = "present";
		var expensive = new Object() {
			int computationCount = 0;

			String compute() {
				computationCount++;
				return "expensive-result";
			}
		};

		// act

		var result = ValidationUtils.requireNonNullElseGet(value, expensive::compute);

		// assert

		assertThat(result).isEqualTo("present");
		assertThat(expensive.computationCount).isEqualTo(0);
	}
}
