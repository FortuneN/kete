package io.github.fortunen.kete.unittests.contenttransferencodings.base64contenttransferencoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.contenttransferencodings.Base64ContentTransferEncoding;

public class encodeTests {

	@Test
	public void shouldEncodeBytes() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();
		var input = "hello world".getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(Base64.getEncoder().encode(input));
	}

	@Test
	public void shouldEncodeEmptyBytes() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();
		var input = new byte[0];

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldEncodeBinaryData() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();
		var input = new byte[] { 0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE };

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result).isNotNull();
		var decoded = Base64.getDecoder().decode(result);
		assertThat(decoded).isEqualTo(input);
	}

	@Test
	public void shouldProduceBase64DecodableOutput() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();
		var input = "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		var decoded = Base64.getDecoder().decode(result);
		assertThat(decoded).isEqualTo(input);
	}

	@Test
	public void shouldThrowWhenBytesIsNull() {

		// arrange

		var encoding = new Base64ContentTransferEncoding();

		// act

		var thrown = catchThrowable(() -> encoding.encode(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("bytes is required");
	}
}
