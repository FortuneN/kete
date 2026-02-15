package io.github.fortunen.kete.unittests.contentencodings.deflatecontentencoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.contentencodings.DeflateContentEncoding;

public class decodeTests {

	@Test
	public void shouldDecompressDeflateBytes() throws Exception {

		// arrange

		var encoding = new DeflateContentEncoding();
		var original = "hello world".getBytes(StandardCharsets.UTF_8);
		var compressed = deflateCompress(original);

		// act

		var result = encoding.decode(compressed);

		// assert

		assertThat(result).isEqualTo(original);
	}

	@Test
	public void shouldDecompressEmptyPayload() throws Exception {

		// arrange

		var encoding = new DeflateContentEncoding();
		var compressed = deflateCompress(new byte[0]);

		// act

		var result = encoding.decode(compressed);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldRoundTripData() {

		// arrange

		var encoding = new DeflateContentEncoding();
		var original = "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8);

		// act

		var compressed = encoding.encode(original);
		var result = encoding.decode(compressed);

		// assert

		assertThat(result).isEqualTo(original);
	}

	@Test
	public void shouldRoundTripBinaryData() {

		// arrange

		var encoding = new DeflateContentEncoding();
		var original = new byte[] { 0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE };

		// act

		var compressed = encoding.encode(original);
		var result = encoding.decode(compressed);

		// assert

		assertThat(result).isEqualTo(original);
	}

	@Test
	public void shouldThrowWhenBytesIsNull() {

		// arrange

		var encoding = new DeflateContentEncoding();

		// act

		var thrown = catchThrowable(() -> encoding.decode(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("bytes is required");
	}

	private static byte[] deflateCompress(byte[] data) throws Exception {

		var outputStream = new ByteArrayOutputStream();

		try (var deflaterStream = new DeflaterOutputStream(outputStream)) {
			deflaterStream.write(data);
		}

		return outputStream.toByteArray();
	}
}
