package io.github.fortunen.kete.contentencodings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.ContentEncoding;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.SneakyThrows;

@Component(name = "gzip", scope = Component.SINGLETON)
public class GzipContentEncoding extends ContentEncoding {

	@Override
	@SneakyThrows
	public byte[] encode(byte[] bytes) {

		ValidationUtils.requireNonNull(bytes, "bytes is required");

		var outputStream = new ByteArrayOutputStream();

		try (var gzipStream = new GZIPOutputStream(outputStream)) {
			gzipStream.write(bytes);
		}

		return outputStream.toByteArray();
	}

	@Override
	@SneakyThrows
	public byte[] decode(byte[] bytes) {

		ValidationUtils.requireNonNull(bytes, "bytes is required");

		try (var inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
			return inputStream.readAllBytes();
		}
	}
}
