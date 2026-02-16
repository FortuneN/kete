package io.github.fortunen.kete.contenttransferencodings;

import java.util.Base64;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.ContentTransferEncoding;
import io.github.fortunen.kete.utils.ValidationUtils;

@Component(name = "base64", scope = Component.SINGLETON)
public class Base64ContentTransferEncoding extends ContentTransferEncoding {

	private static final Base64.Encoder ENCODER = Base64.getEncoder();
	private static final Base64.Decoder DECODER = Base64.getDecoder();

	@Override
	public byte[] encode(byte[] bytes) {

		ValidationUtils.requireNonNull(bytes, "bytes is required");

		return ENCODER.encode(bytes);
	}

	@Override
	public byte[] decode(byte[] bytes) {

		ValidationUtils.requireNonNull(bytes, "bytes is required");

		return DECODER.decode(bytes);
	}
}
