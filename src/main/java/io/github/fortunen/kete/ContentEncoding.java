package io.github.fortunen.kete;

public abstract class ContentEncoding {
	public abstract byte[] encode(byte[] bytes);
	public abstract byte[] decode(byte[] bytes);
}
