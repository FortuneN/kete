package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

public class requireNonBlankElseGetTests {

	@Test
	public void shouldReturnValueWhenNonBlank() {

		// arrange

		var value = "test";
		var invoked = new boolean[] { false };

		// act

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> {
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

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> {
			invoked[0] = true;
			return "default";
		});

		// assert

		assertThat(result).isEqualTo("default");
		assertThat(invoked[0]).isEqualTo(true);
	}

	@Test
	public void shouldReturnSupplierValueWhenEmpty() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> "default");

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnSupplierValueWhenWhitespace() {

		// arrange

		var value = "   ";

		// act

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> "default");

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnValueWhenHasContent() {

		// arrange

		var value = "  test  ";

		// act

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> "default");

		// assert

		assertThat(result).isEqualTo("  test  ");
	}

	@Test
	public void shouldInvokeSupplierOnlyWhenNeeded() {

		// arrange

		var value = "test";
		var invocationCount = new int[] { 0 };

		// act

		var result1 = ValidationUtils.requireNonBlankElseGet(value, () -> {
			invocationCount[0]++;
			return "default";
		});

		var result2 = ValidationUtils.requireNonBlankElseGet("", () -> {
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

		var value = "";

		// act

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> {
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

		var result = ValidationUtils.requireNonBlankElseGet(value, expensive::compute);

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

		var result = ValidationUtils.requireNonBlankElseGet(value, expensive::compute);

		// assert

		assertThat(result).isEqualTo("present");
		assertThat(expensive.computationCount).isEqualTo(0);
	}

	@Test
	public void shouldReturnSupplierValueForTabsAndNewlines() {

		// arrange

		var value = "\t\n\r";

		// act

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> "default");

		// assert

		assertThat(result).isEqualTo("default");
	}

	@Test
	public void shouldReturnNullFromSupplier() {

		// arrange

		var value = "";

		// act

		var result = ValidationUtils.requireNonBlankElseGet(value, () -> null);

		// assert

		assertThat(result).isEqualTo(null);
	}
}
