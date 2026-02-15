package io.github.fortunen.kete.contentencodings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.ContentEncoding;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.SneakyThrows;

@Component(name = "deflate", scope = Component.SINGLETON)
public class DeflateContentEncoding extends ContentEncoding {

	@Override
	@SneakyThrows
	public byte[] encode(byte[] bytes) {

		ValidationUtils.requireNonNull(bytes, "bytes is required");

		var outputStream = new ByteArrayOutputStream();

		try (var deflaterStream = new DeflaterOutputStream(outputStream)) {
			deflaterStream.write(bytes);
		}

		return outputStream.toByteArray();
	}

	@Override
	@SneakyThrows
	public byte[] decode(byte[] bytes) {

		ValidationUtils.requireNonNull(bytes, "bytes is required");

		try (var inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes))) {
			return inputStream.readAllBytes();
		}
	}
}
