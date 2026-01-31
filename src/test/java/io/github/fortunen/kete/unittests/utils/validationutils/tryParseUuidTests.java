package io.github.fortunen.kete.unittests.utils.validationutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.ValidationUtils;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class tryParseUuidTests {

	@Test
	public void shouldReturnEmptyForNull() {

		// act & assert

		assertThat(ValidationUtils.tryParseUuid(null)).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForEmptyString() {

		// act & assert

		assertThat(ValidationUtils.tryParseUuid("")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForBlankString() {

		// act & assert

		assertThat(ValidationUtils.tryParseUuid("   ")).isEmpty();
	}

	@Test
	public void shouldParseValidUuid() {

		// arrange

		var uuid = "550e8400-e29b-41d4-a716-446655440000";

		// act & assert

		assertThat(ValidationUtils.tryParseUuid(uuid)).hasValue(UUID.fromString(uuid));
	}

	@Test
	public void shouldTrimWhitespace() {

		// arrange

		var uuid = "550e8400-e29b-41d4-a716-446655440000";

		// act & assert

		assertThat(ValidationUtils.tryParseUuid("  " + uuid + "  ")).hasValue(UUID.fromString(uuid));
	}

	@Test
	public void shouldParseUppercaseUuid() {

		// arrange

		var uuid = "550E8400-E29B-41D4-A716-446655440000";

		// act & assert

		assertThat(ValidationUtils.tryParseUuid(uuid)).hasValue(UUID.fromString(uuid));
	}

	@Test
	public void shouldParseNilUuid() {

		// arrange

		var nilUuid = "00000000-0000-0000-0000-000000000000";

		// act & assert

		assertThat(ValidationUtils.tryParseUuid(nilUuid)).hasValue(UUID.fromString(nilUuid));
	}

	@Test
	public void shouldReturnEmptyForInvalidFormat() {

		// act & assert

		assertThat(ValidationUtils.tryParseUuid("not-a-uuid")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForTooShort() {

		// act & assert

		assertThat(ValidationUtils.tryParseUuid("550e8400-e29b-41d4")).isEmpty();
	}

	@Test
	public void shouldReturnEmptyForMissingHyphens() {

		// act & assert

		assertThat(ValidationUtils.tryParseUuid("550e8400e29b41d4a716446655440000")).isEmpty();
	}
}
