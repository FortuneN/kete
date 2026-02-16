package io.github.fortunen.kete.unittests.contentencodings.gzipcontentencoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;

import io.github.fortunen.kete.contentencodings.GzipContentEncoding;

public class encodeTests {

	@Test
	public void shouldCompressBytes() {

		// arrange

		var encoding = new GzipContentEncoding();
		var input = "hello world".getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	public void shouldProduceValidGzipOutput() throws Exception {

		// arrange

		var encoding = new GzipContentEncoding();
		var input = "hello world".getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		try (var gzipStream = new GZIPInputStream(new ByteArrayInputStream(result))) {
			var decompressed = gzipStream.readAllBytes();
			assertThat(decompressed).isEqualTo(input);
		}
	}

	@Test
	public void shouldCompressEmptyBytes() {

		// arrange

		var encoding = new GzipContentEncoding();
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

		var encoding = new GzipContentEncoding();
		var input = "A".repeat(10000).getBytes(StandardCharsets.UTF_8);

		// act

		var result = encoding.encode(input);

		// assert

		assertThat(result.length).isLessThan(input.length);

		try (var gzipStream = new GZIPInputStream(new ByteArrayInputStream(result))) {
			var decompressed = gzipStream.readAllBytes();
			assertThat(decompressed).isEqualTo(input);
		}
	}

	@Test
	public void shouldThrowWhenBytesIsNull() {

		// arrange

		var encoding = new GzipContentEncoding();

		// act

		var thrown = catchThrowable(() -> encoding.encode(null));

		// assert

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		assertThat(thrown).hasMessageContaining("bytes is required");
	}
}
