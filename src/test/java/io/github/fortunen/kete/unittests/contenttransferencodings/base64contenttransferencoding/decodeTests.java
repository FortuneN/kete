package io.github.fortunen.kete.unittests.contenttransferencodings.base64contenttransferencoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.contenttransferencodings.Base64ContentTransferEncoding;

public class decodeTests {

	@Test
	public void shouldDecodeBase64Bytes() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();
		var original = "hello world".getBytes(StandardCharsets.UTF_8);
		var encoded = Base64.getEncoder().encode(original);

		// act

		var result = encoding.decode(encoded);

		// assert

		assertThat(result).isEqualTo(original);
	}

	@Test
	public void shouldDecodeEmptyBytes() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();
		var input = new byte[0];

		// act

		var result = encoding.decode(input);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldRoundTripBinaryData() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();
		var original = new byte[] { 0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE };

		// act

		var encoded = encoding.encode(original);
		var result = encoding.decode(encoded);

		// assert

		assertThat(result).isEqualTo(original);
	}

	@Test
	public void shouldThrowWhenBytesIsNull() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();

		// act

		var thrown = catchThrowable(() -> encoding.decode(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("bytes is required");
	}
}
