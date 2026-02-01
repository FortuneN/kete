package io.github.fortunen.kete.unittests.utils.base64utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.fortunen.kete.utils.Base64Utils;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class encodeTests {

	@Test
	public void shouldEncodeValidBytes() {

		// arrange

		var bytes = "Hello World".getBytes(StandardCharsets.UTF_8);

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("SGVsbG8gV29ybGQ=");
	}

	@Test
	public void shouldEncodeEmptyBytes() {

		// arrange

		var bytes = new byte[0];

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldEncodeBinaryData() {

		// arrange

		var bytes = new byte[] { 0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE };

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(Base64Utils.decode(result)).isEqualTo(bytes);
	}

	@Test
	public void shouldEncodeUnicodeContent() {

		// arrange

		var original = "こんにちは世界"; // Japanese "Hello World"
		var bytes = original.getBytes(StandardCharsets.UTF_8);

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(Base64Utils.decode(result), StandardCharsets.UTF_8)).isEqualTo("こんにちは世界");
	}

	@Test
	public void shouldEncodeLongByteArray() {

		// arrange

		var original = "A".repeat(1000);
		var bytes = original.getBytes(StandardCharsets.UTF_8);

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(Base64Utils.decode(result), StandardCharsets.UTF_8)).isEqualTo(original);
	}

	@Test
	public void shouldThrowWhenInputIsNull() {

		// arrange

		byte[] bytes = null;

		// act

		var thrown = catchThrowable(() -> {
			Base64Utils.encode(bytes);
		});

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown.getMessage()).isEqualTo("bytes is required");
	}

	@Test
	public void shouldEncodeSpecialCharactersInContent() {

		// arrange

		var original = "Test with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
		var bytes = original.getBytes(StandardCharsets.UTF_8);

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(Base64Utils.decode(result), StandardCharsets.UTF_8)).isEqualTo(original);
	}

	@Test
	public void shouldEncodeSingleByte() {

		// arrange

		var bytes = new byte[] { 0x41 }; // 'A'

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("QQ==");
	}

	@Test
	public void shouldEncodeTwoBytes() {

		// arrange

		var bytes = new byte[] { 0x41, 0x42 }; // 'AB'

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("QUI=");
	}

	@Test
	public void shouldEncodeThreeBytes() {

		// arrange

		var bytes = new byte[] { 0x41, 0x42, 0x43 }; // 'ABC'

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("QUJD");
	}

	@Test
	public void shouldRoundTripEncodeDecode() {

		// arrange

		var original = "The quick brown fox jumps over the lazy dog";
		var bytes = original.getBytes(StandardCharsets.UTF_8);

		// act

		var encoded = Base64Utils.encode(bytes);
		var decoded = Base64Utils.decode(encoded);

		// assert

		assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(original);
	}

	@Test
	public void shouldEncodeAllByteValues() {

		// arrange

		var bytes = new byte[256];
		for (int i = 0; i < 256; i++) {
			bytes[i] = (byte) i;
		}

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(Base64Utils.decode(result)).isEqualTo(bytes);
	}

	@Test
	public void shouldEncodeNewlinesInContent() {

		// arrange

		var original = "Line1\nLine2\r\nLine3";
		var bytes = original.getBytes(StandardCharsets.UTF_8);

		// act

		var result = Base64Utils.encode(bytes);

		// assert

		assertThat(result).isNotNull();
		assertThat(new String(Base64Utils.decode(result), StandardCharsets.UTF_8)).isEqualTo(original);
	}
}
