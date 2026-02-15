package io.github.fortunen.kete.unittests.contentencodings.deflatecontentencoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.InflaterInputStream;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.contentencodings.DeflateContentEncoding;

public class encodeTests {

	@Test
	public void shouldCompressBytes() {

		// arrange

		var encoding = new DeflateContentEncoding();
		var input = "hello world".getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	public void shouldProduceValidDeflateOutput() throws Exception {

		// arrange

		var encoding = new DeflateContentEncoding();
		var input = "hello world".getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		try (var inflaterStream = new InflaterInputStream(new ByteArrayInputStream(result))) {
			var decompressed = inflaterStream.readAllBytes();
			assertThat(decompressed).isEqualTo(input);
		}
	}

	@Test
	public void shouldCompressEmptyBytes() {

		// arrange

		var encoding = new DeflateContentEncoding();
		var input = new byte[0];

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	public void shouldCompressLargePayload() throws Exception {

		// arrange

		var encoding = new DeflateContentEncoding();
		var input = "A".repeat(10000).getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result.length).isLessThan(input.length);

		try (var inflaterStream = new InflaterInputStream(new ByteArrayInputStream(result))) {
			var decompressed = inflaterStream.readAllBytes();
			assertThat(decompressed).isEqualTo(input);
		}
	}

	@Test
	public void shouldThrowWhenBytesIsNull() {

		// arrange

		var encoding = new DeflateContentEncoding();

		// act

		var thrown = catchThrowable(() -> encoding.encode(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("bytes is required");
	}
}
