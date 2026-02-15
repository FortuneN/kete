package io.github.fortunen.kete.unittests.contentencodings.gzipcontentencoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.contentencodings.GzipContentEncoding;

public class decodeTests {

	@Test
	public void shouldDecompressGzipBytes() throws Exception {

		// arrange

		var encoding = new GzipContentEncoding();
		var original = "hello world".getBytes(StandardCharsets.UTF_8);
		var compressed = gzipCompress(original);

		// act

		var result = encoding.decode(compressed);

		// assert

		assertThat(result).isEqualTo(original);
	}

	@Test
	public void shouldDecompressEmptyPayload() throws Exception {

		// arrange

		var encoding = new GzipContentEncoding();
		var compressed = gzipCompress(new byte[0]);

		// act

		var result = encoding.decode(compressed);

		// assert

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldRoundTripData() {

		// arrange

		var encoding = new GzipContentEncoding();
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

		var encoding = new GzipContentEncoding();
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

		var encoding = new GzipContentEncoding();

		// act

		var thrown = catchThrowable(() -> encoding.decode(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("bytes is required");
	}

	private static byte[] gzipCompress(byte[] data) throws Exception {

		var outputStream = new ByteArrayOutputStream();

		try (var gzipStream = new GZIPOutputStream(outputStream)) {
			gzipStream.write(data);
		}

		return outputStream.toByteArray();
	}
}
