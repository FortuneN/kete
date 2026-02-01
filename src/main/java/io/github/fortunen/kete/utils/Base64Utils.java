package io.github.fortunen.kete.utils;

import java.util.Base64;

public final class Base64Utils {

	private static final Base64.Decoder DECODER = Base64.getMimeDecoder();
	private static final Base64.Encoder ENCODER = Base64.getMimeEncoder();

	private Base64Utils() {}

	public static byte[] decode(String base64) {

		ValidationUtils.requireNonNull(base64, "base64 is required");

		return DECODER.decode(base64);
	}

	public static String encode(byte[] bytes) {

		ValidationUtils.requireNonNull(bytes, "bytes is required");

		return ENCODER.encodeToString(bytes);
	}
}
