package io.github.fortunen.kete.unittests.utils.base64utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.Base64Utils;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class decodeTests {

	@Test
	public void shouldDecodeValidBase64String() {

		// arrange

		var base64 = "SGVsbG8gV29ybGQ="; // "Hello World"

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello World");
	}

	@Test
	public void shouldDecodeEmptyString() {

		// arrange

		var base64 = "";

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldDecodeBase64WithNewlines() {

		// arrange

		var base64 = "SGVs\nbG8g\nV29y\nbGQ="; // "Hello World" with newlines (MIME format)

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello World");
	}

	@Test
	public void shouldDecodeBase64WithCarriageReturnAndNewline() {

		// arrange

		var base64 = "SGVs\r\nbG8g\r\nV29y\r\nbGQ="; // "Hello World" with CRLF

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello World");
	}

	@Test
	public void shouldDecodeBase64WithSpaces() {

		// arrange

		var base64 = "SGVs bG8g V29y bGQ="; // "Hello World" with spaces

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello World");
	}

	@Test
	public void shouldDecodeBinaryData() {

		// arrange

		var originalBytes = new byte[] { 0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE };
		var base64 = java.util.Base64.getEncoder().encodeToString(originalBytes);

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(originalBytes);
	}

	@Test
	public void shouldDecodeUnicodeContent() {

		// arrange

		var original = "こんにちは世界"; // Japanese "Hello World"
		var base64 = java.util.Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("こんにちは世界");
	}

	@Test
	public void shouldDecodeLongBase64String() {

		// arrange

		var original = "A".repeat(1000);
		var base64 = java.util.Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(original);
	}

	@Test
	public void shouldDecodeBase64WithoutPadding() {

		// arrange

		var base64 = "SGVsbG8"; // "Hello" without padding (MIME decoder accepts this)

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello");
	}

	@Test
	public void shouldThrowWhenInputIsNull() {

		// arrange

		String base64 = null;

		// act

		var thrown = catchThrowable(() -> {
			Base64Utils.decode(base64);
		});

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("base64 is required");
	}

	@Test
	public void shouldDecodeSpecialCharactersInContent() {

		// arrange

		var original = "Test with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
		var base64 = java.util.Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(original);
	}

	@Test
	public void shouldDecodeSingleCharacter() {

		// arrange

		var base64 = "QQ=="; // "A"

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("A");
	}

	@Test
	public void shouldDecodeTwoCharacters() {

		// arrange

		var base64 = "QUI="; // "AB"

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("AB");
	}

	@Test
	public void shouldDecodeThreeCharacters() {

		// arrange

		var base64 = "QUJD"; // "ABC"

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("ABC");
	}

	@Test
	public void shouldDecodeBase64WithTabCharacters() {

		// arrange

		var base64 = "SGVs\tbG8g\tV29y\tbGQ="; // "Hello World" with tabs

		// act

		var result = Base64Utils.decode(base64);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello World");
	}
}
